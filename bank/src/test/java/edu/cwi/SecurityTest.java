package edu.cwi;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.ibatis.session.SqlSession;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cwi.data.AccountMapper;
import edu.cwi.data.MyBatisConfig;
import edu.cwi.data.TransactionMapper;
import edu.cwi.data.UserMapper;
import io.javalin.Javalin;

public class SecurityTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String ADMIN_PASSWORD = "AdminPass123!";
    private static final String ADMIN_HASH = Passwords.hash(ADMIN_PASSWORD);

    private JdbcDataSource source;
    private MyBatisConfig database;
    private Javalin app;
    private String baseUrl;

    @Before
    public void start() throws Exception {
        source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:bank_" + UUID.randomUUID()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        source.setUser("sa");

        String sql = Files.readString(Path.of("database", "online_bank.sql"))
                .replaceAll("(?m)^\\s*--.*$", "");

        try (Connection connection = source.getConnection();
             Statement statement = connection.createStatement()) {
            for (String part : sql.split(";")) {
                String command = part.trim();

                if (command.isEmpty()
                        || command.startsWith("DROP DATABASE")
                        || command.startsWith("CREATE DATABASE")
                        || command.startsWith("USE ")) {
                    continue;
                }

                statement.execute(command);
            }
        }

        database = new MyBatisConfig(source);
        database.write(session -> {
            UserMapper mapper = session.getMapper(UserMapper.class);
            User admin = mapper.findByUsername("admin");

            assertFalse(Passwords.matches(ADMIN_PASSWORD, admin.getPassword()));
            admin.setPassword(ADMIN_HASH);
            mapper.update(admin);
            return null;
        });

        app = Main.createApp(database).start(0);
        baseUrl = "http://localhost:" + app.port();
    }

    @After
    public void stop() throws Exception {
        if (app != null) {
            app.stop();
        }

        if (source != null) {
            try (Connection connection = source.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute("SHUTDOWN");
            }
        }
    }

    private final class Browser {
        final CookieManager cookies =
                new CookieManager(null, CookiePolicy.ACCEPT_ALL);

        final HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        String csrf;

        HttpResponse<String> request(
                String method, String path, String body,
                String contentType, boolean withToken) throws Exception {
            HttpRequest.Builder builder =
                    HttpRequest.newBuilder(URI.create(baseUrl + path));

            if (contentType != null) {
                builder.header("Content-Type", contentType);
            }

            if (withToken && csrf != null) {
                builder.header("X-CSRF-Token", csrf);
            }

            builder.method(method, body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body));

            return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        }

        HttpResponse<String> get(String path) throws Exception {
            return request("GET", path, null, null, false);
        }

        HttpResponse<String> json(String method, String path, Object body)
                throws Exception {
            String jsonBody = body == null ? null : JSON.writeValueAsString(body);
            return request(method, path, jsonBody, "application/json", true);
        }

        HttpResponse<String> form(String path, Map<String, String> fields)
                throws Exception {
            String body = fields.entrySet().stream()
                    .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                    .collect(Collectors.joining("&"));

            return request("POST", path, body,
                    "application/x-www-form-urlencoded", true);
        }

        void login(String username, String password) throws Exception {
            HttpResponse<String> token = get("/auth/csrf");
            assertEquals(token.body(), 200, token.statusCode());
            csrf = JSON.readTree(token.body()).get("csrfToken").asText();

            HttpResponse<String> login = form("/login?return=json",
                    Map.of("username", username, "password", password));

            assertEquals(login.body(), 200, login.statusCode());
            csrf = JSON.readTree(login.body()).get("csrfToken").asText();
        }

        String cookie() {
            return cookies.getCookieStore().getCookies().stream()
                    .filter(cookie -> cookie.getName().equals("cwi_session"))
                    .findFirst()
                    .orElseThrow()
                    .getValue();
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private Browser admin() throws Exception {
        Browser browser = new Browser();
        browser.login("admin", ADMIN_PASSWORD);
        return browser;
    }

    private Map<String, Object> newUser(String username) {
        return Map.of(
                "username", username,
                "password", "Password123!",
                "email", username + "@example.com",
                "firstName", username,
                "lastName", "Customer",
                "role", "CUSTOMER"
        );
    }

    private long create(Browser admin, String username) throws Exception {
        HttpResponse<String> response =
                admin.json("POST", "/users?return=json", newUser(username));

        assertEquals(response.body(), 201, response.statusCode());
        assertFalse(response.body(), response.body().contains("password"));
        return JSON.readTree(response.body()).get("id").asLong();
    }

    private Account account(long userId) {
        try (SqlSession session = database.openSession()) {
            return session.getMapper(AccountMapper.class)
                    .findByUserId(userId)
                    .get(0);
        }
    }

    private long count(String table) throws Exception {
        try (Connection connection = source.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rows.next();
            return rows.getLong(1);
        }
    }

    @Test
    public void usersCrudReturnsJsonAndNeverReturnsPasswords() throws Exception {
        Browser admin = admin();
        long id = create(admin, "enock");

        assertEquals(2, count("users"));
        assertEquals(1, count("accounts"));
        assertEquals(1, count("transactions"));
        assertEquals("5.00", account(id).getBalance().toPlainString());

        User stored = new UsersController(database).findUser(id);
        assertTrue(stored.getPassword().startsWith("$2a$"));
        assertTrue(Passwords.matches("Password123!", stored.getPassword()));

        HttpResponse<String> all = admin.get("/users");
        assertEquals(200, all.statusCode());
        assertTrue(all.headers().firstValue("Content-Type")
                .orElse("").startsWith("application/json"));
        assertFalse(all.body().contains("password"));
        assertFalse(all.body().contains("$2a$"));

        HttpResponse<String> one = admin.get("/users/" + id);
        assertEquals(200, one.statusCode());
        assertEquals("enock", JSON.readTree(one.body()).get("username").asText());

        HttpResponse<String> update = admin.json("PATCH", "/users/" + id,
                Map.of("firstName", "Updated"));
        assertEquals(update.body(), 200, update.statusCode());
        assertEquals("Updated", JSON.readTree(update.body()).get("firstName").asText());

        HttpResponse<String> deleted = admin.json("DELETE", "/users/" + id, null);
        assertEquals(deleted.body(), 200, deleted.statusCode());
        assertTrue(JSON.readTree(deleted.body()).get("deleted").asBoolean());

        assertEquals(1, count("users"));
        assertEquals(0, count("accounts"));
        assertEquals(0, count("transactions"));
        assertEquals(404, admin.get("/users/" + id).statusCode());
    }

    @Test
    public void finalInsertFailureRollsBackUserAndAccount() throws Exception {
        Browser admin = admin();

        try (Connection connection = source.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(
                    "ALTER TABLE transactions ADD CONSTRAINT test_maximum CHECK(amount < 5)");
        }

        HttpResponse<String> response = admin.json("POST", "/users", newUser("rollback"));
        assertEquals(response.body(), 409, response.statusCode());

        assertEquals(1, count("users"));
        assertEquals(0, count("accounts"));
        assertEquals(0, count("transactions"));

        try (SqlSession session = database.openSession()) {
            assertNull(session.getMapper(UserMapper.class).findByUsername("rollback"));
        }
    }

    @Test
    public void validationAndDuplicatesDoNotCreatePartialRecords() throws Exception {
        Browser admin = admin();
        create(admin, "duplicate");

        assertEquals(409, admin.json("POST", "/users", newUser("duplicate")).statusCode());
        assertEquals(400, admin.json("POST", "/users", newUser("")).statusCode());
        assertEquals(400, admin.json("POST", "/users", newUser("a".repeat(51))).statusCode());

        Map<String, Object> missing = new HashMap<>(newUser("missing"));
        missing.remove("email");
        assertEquals(400, admin.json("POST", "/users", missing).statusCode());

        assertEquals(2, count("users"));
        assertEquals(1, count("accounts"));
        assertEquals(1, count("transactions"));
    }

    @Test
    public void databaseLoginRolesAndCookieForgeryAreChecked() throws Exception {
        Browser visitor = new Browser();
        assertEquals(401, visitor.get("/users").statusCode());

        HttpResponse<String> invalidSession = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/users"))
                        .header("Cookie", "cwi_session=invalid-test-session")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(401, invalidSession.statusCode());

        Browser admin = admin();
        create(admin, "alice");

        Browser alice = new Browser();
        alice.login("alice", "Password123!");
        assertEquals(403, alice.get("/users").statusCode());
        assertEquals(403, alice.json("POST", "/users", newUser("illegal")).statusCode());

        HttpResponse<String> token = visitor.get("/auth/csrf");
        visitor.csrf = JSON.readTree(token.body()).get("csrfToken").asText();

        assertEquals(401, visitor.form("/login",
                Map.of("username", "alice", "password", "wrong")).statusCode());

        assertEquals(403, admin.request("POST", "/users",
                JSON.writeValueAsString(newUser("csrf")),
                "application/json", false).statusCode());
    }

    @Test
    public void layoutsLedgerAndAccountOwnershipUseDatabaseRecords() throws Exception {
        Browser admin = admin();
        long enockId = create(admin, "enock");
        long aliceId = create(admin, "alice");
        Account enockAccount = account(enockId);

        Browser enock = new Browser();
        enock.login("enock", "Password123!");

        Browser alice = new Browser();
        alice.login("alice", "Password123!");

        HttpResponse<String> page = enock.get("/account");
        assertEquals(page.body(), 200, page.statusCode());
        assertTrue(page.body(), page.body().contains("enock Customer"));
        assertTrue(page.body(), page.body().contains("$5.00"));
        assertTrue(page.body(), page.body().contains("<title>My Account</title>"));
        assertTrue(page.body(), page.body().contains("name=\"csrfToken\""));

        assertEquals(404, alice.get("/account?accountId=" + enockAccount.getId()).statusCode());
        assertEquals(404, alice.form("/transactions", Map.of(
                "accountId", enockAccount.getId().toString(),
                "description", "Not mine",
                "amount", "1"
        )).statusCode());

        assertEquals(303, enock.form("/transactions", Map.of(
                "accountId", enockAccount.getId().toString(),
                "description", "Gift <script>demo</script>",
                "amount", "25"
        )).statusCode());

        assertEquals("30.00", account(enockId).getBalance().toPlainString());
        assertTrue(enock.get("/account").body().contains("&lt;script&gt;"));

        Transaction created;
        try (SqlSession session = database.openSession()) {
            List<Transaction> rows = session.getMapper(TransactionMapper.class)
                    .findByAccountId(enockAccount.getId());
            created = rows.get(rows.size() - 1);
        }

        String transactionPath = "/transactions/" + created.getId();
        assertEquals(404, alice.get(transactionPath).statusCode());
        assertEquals(200, enock.get(transactionPath).statusCode());

        assertEquals(303, enock.form(transactionPath + "/update",
                Map.of("description", "Gift", "amount", "30")).statusCode());
        assertEquals("35.00", account(enockId).getBalance().toPlainString());

        assertEquals(303, enock.form(transactionPath + "/delete", Map.of()).statusCode());
        assertEquals("5.00", account(enockId).getBalance().toPlainString());
        assertEquals("5.00", account(aliceId).getBalance().toPlainString());

        assertEquals(400, enock.form("/transactions", Map.of(
                "accountId", enockAccount.getId().toString(),
                "description", "Too much",
                "amount", "-1000"
        )).statusCode());

        assertEquals("5.00", account(enockId).getBalance().toPlainString());
    }

    @Test
    public void logoutPasswordChangeAndUserDeletionInvalidateSessions() throws Exception {
        Browser admin = admin();
        long id = create(admin, "customer");

        Browser customer = new Browser();
        customer.login("customer", "Password123!");
        String oldCookie = customer.cookie();

        assertEquals(403, customer.request("POST", "/logout", null, null, false).statusCode());
        assertEquals(303, customer.form("/logout", Map.of()).statusCode());

        HttpResponse<String> afterLogout = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/account"))
                        .header("Cookie", "cwi_session=" + oldCookie)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(303, afterLogout.statusCode());
        assertEquals("/login", afterLogout.headers().firstValue("Location").orElse(""));

        customer.login("customer", "Password123!");
        assertEquals(200, admin.json("PATCH", "/users/" + id,
                Map.of("password", "Replacement123!")).statusCode());
        assertEquals(303, customer.get("/account").statusCode());

        customer.login("customer", "Replacement123!");
        assertEquals(200, admin.json("DELETE", "/users/" + id, null).statusCode());
        assertEquals(303, customer.get("/account").statusCode());

        assertEquals(409, admin.json("DELETE", "/users/1", null).statusCode());
    }
}