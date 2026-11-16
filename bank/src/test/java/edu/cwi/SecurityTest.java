package edu.cwi;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import io.javalin.Javalin;

public class SecurityTest {
    private Javalin app;
    private String baseUrl;

    @Before
    public void startApp() {
        app = Main.createApp().start(0);
        baseUrl = "http://localhost:" + app.port();
    }

    @After
    public void stopApp() {
        if (app != null) {
            app.stop();
        }
    }

    private final class Browser {
        private final CookieManager cookies =
                new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        private final HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookies)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        HttpResponse<String> request(String method, String path,
                                    Map<String, String> fields)
                throws IOException, InterruptedException {
            String body = fields.entrySet().stream()
                    .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                    .collect(Collectors.joining("&"));
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + path));
            if (method.equals("GET")) {
                request.GET();
            } else {
                request.header("Content-Type", "application/x-www-form-urlencoded")
                        .method(method, HttpRequest.BodyPublishers.ofString(body));
            }
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        }

        HttpResponse<String> get(String path) throws Exception {
            return request("GET", path, Map.of());
        }

        HttpResponse<String> post(String path, Map<String, String> fields) throws Exception {
            return request("POST", path, fields);
        }

        String cookie() {
            return cookies.getCookieStore().getCookies().stream()
                    .filter(c -> c.getName().equals("cwi_session"))
                    .findFirst().orElseThrow().getValue();
        }

        String login(String username, String password) throws Exception {
            String token = token(get("/login").body());
            String anonymousId = cookie();
            HttpResponse<String> response = post("/login", Map.of(
                    "username", username, "password", password, "csrfToken", token));
            redirect(response, "/account");
            assertFalse("Login must rotate the session ID", anonymousId.equals(cookie()));
            return token(get("/account").body());
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String token(String html) {
        Matcher matcher = Pattern.compile(
                "name=\"csrfToken\" value=\"([^\"]+)\"").matcher(html);
        assertTrue("Page needs a hidden form token", matcher.find());
        return matcher.group(1);
    }

    private static void redirect(HttpResponse<String> response, String location) {
        assertEquals(response.body(), 303, response.statusCode());
        assertEquals(location, response.headers().firstValue("Location").orElse(""));
    }

    private HttpResponse<String> withCookie(String cookie, String path) throws Exception {
        return HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(baseUrl + path))
                        .header("Cookie", "cwi_session=" + cookie).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void anonymousVisitorsCannotUseProtectedRoutes() throws Exception {
        Browser visitor = new Browser();
        for (String path : new String[] {
                "/account", "/account.html", "/transactions", "/transactions/1"}) {
            redirect(visitor.get(path), "/login");
        }
        for (String path : new String[] {
                "/transactions", "/transactions/1/update",
                "/transactions/1/delete", "/logout"}) {
            redirect(visitor.post(path, Map.of()), "/login");
        }
        redirect(visitor.request("PATCH", "/transactions/1", Map.of()), "/login");
        redirect(visitor.request("DELETE", "/transactions/1", Map.of()), "/login");
        redirect(withCookie("enock", "/account"), "/login");
    }

    @Test
    public void loginRejectsBadCredentialsAndMissingFormToken() throws Exception {
        Browser visitor = new Browser();
        HttpResponse<String> page = visitor.get("/login");
        String cookieHeader = page.headers().firstValue("Set-Cookie").orElse("");
        assertTrue(cookieHeader, cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader, cookieHeader.contains("SameSite=Lax"));
        assertTrue(cookieHeader, cookieHeader.contains("Path=/"));
        assertTrue(cookieHeader, cookieHeader.contains("Max-Age=1800"));
        assertFalse(cookieHeader, cookieHeader.contains("Enock123!"));

        assertEquals(403, visitor.post("/login", Map.of(
                "username", "enock", "password", "Enock123!")).statusCode());
        HttpResponse<String> bad = visitor.post("/login", Map.of(
                "username", "enock", "password", "wrong",
                "csrfToken", token(page.body())));
        assertEquals(401, bad.statusCode());
        assertTrue(bad.body().contains("Incorrect username or password."));
        redirect(visitor.get("/account"), "/login");
    }

    @Test
    public void eachUserSeesTheirOwnAccountThroughTheLayout() throws Exception {
        Browser enock = new Browser();
        Browser alice = new Browser();
        enock.login("enock", "Enock123!");
        alice.login("alice", "Alice123!");
        String first = enock.get("/account").body();
        String second = alice.get("/account").body();

        assertTrue(first, first.contains("<title>My Account</title>"));
        assertTrue(first, first.contains("Enock's account"));
        assertTrue(first, first.contains("$5,250.50"));
        assertTrue(first, first.contains("Salary"));
        assertFalse(first, first.contains("Books"));
        assertTrue(second, second.contains("Alice's account"));
        assertTrue(second, second.contains("$2,914.25"));
        assertTrue(second, second.contains("Books"));
        assertFalse(second, second.contains("Salary"));
        assertEquals(200, enock.get("/transactions").statusCode());
        assertTrue(first, first.contains("action=\"/logout\""));
        assertEquals("no-store", enock.get("/account").headers()
                .firstValue("Cache-Control").orElse(""));
    }

    @Test
    public void formsUpdateOnlyTheLoggedInUsersLedger() throws Exception {
        Browser enock = new Browser();
        Browser alice = new Browser();
        String enockToken = enock.login("enock", "Enock123!");
        String aliceToken = alice.login("alice", "Alice123!");

        assertEquals(403, enock.post("/transactions", Map.of(
                "description", "Blocked", "amount", "10")).statusCode());
        assertEquals(403, enock.post("/transactions", Map.of(
                "description", "Blocked", "amount", "10",
                "csrfToken", aliceToken)).statusCode());

        redirect(enock.post("/transactions", Map.of(
                "description", "Gift <script>demo</script>", "amount", "25",
                "csrfToken", enockToken)), "/account");
        String page = enock.get("/account").body();
        assertTrue(page, page.contains("$1,275.50"));
        assertTrue(page, page.contains("Gift &lt;script&gt;demo&lt;/script&gt;"));
        assertFalse(page, page.contains("<script>"));
        assertEquals(404, alice.get("/transactions/3").statusCode());

        assertEquals(404, alice.post("/transactions/3/update", Map.of(
                "description", "Other user", "amount", "1",
                "csrfToken", aliceToken)).statusCode());
        assertEquals(404, alice.post("/transactions/3/delete",
                Map.of("csrfToken", aliceToken)).statusCode());
        assertFalse(alice.get("/account").body().contains("Gift"));

        assertEquals(enockToken, token(enock.get("/transactions/3").body()));
        redirect(enock.post("/transactions/3/update", Map.of(
                "description", "Gift", "amount", "30",
                "csrfToken", enockToken)), "/account");
        assertTrue(enock.get("/account").body().contains("$1,280.50"));

        redirect(enock.request("PATCH", "/transactions/3", Map.of(
                "description", "Gift", "amount", "40",
                "csrfToken", enockToken)), "/account");
        assertTrue(enock.get("/account").body().contains("$1,290.50"));

        redirect(enock.request("DELETE", "/transactions/3",
                Map.of("csrfToken", enockToken)), "/account");
        assertTrue(enock.get("/account").body().contains("$1,250.50"));

        redirect(enock.post("/transactions", Map.of(
                "description", "Temporary", "amount", "1",
                "csrfToken", enockToken)), "/account");
        redirect(enock.post("/transactions/4/delete",
                Map.of("csrfToken", enockToken)), "/account");
        assertTrue(alice.get("/account").body().contains("$1,914.25"));
    }

    @Test
    public void bothUsersCanLogOutAndOldCookiesStopWorking() throws Exception {
        for (String[] credentials : new String[][] {
                {"enock", "Enock123!"}, {"alice", "Alice123!"}}) {
            Browser browser = new Browser();
            String token = browser.login(credentials[0], credentials[1]);
            String oldCookie = browser.cookie();
            redirect(withCookie(oldCookie + "tampered", "/account"), "/login");
            assertEquals(403, browser.post("/logout", Map.of()).statusCode());
            assertEquals(200, browser.get("/account").statusCode());

            HttpResponse<String> logout = browser.post(
                    "/logout", Map.of("csrfToken", token));
            redirect(logout, "/login");
            String removedCookie = logout.headers().firstValue("Set-Cookie").orElse("");
            assertEquals(removedCookie, 0,
                    HttpCookie.parse(removedCookie).get(0).getMaxAge());
            redirect(browser.get("/account"), "/login");
            redirect(withCookie(oldCookie, "/account"), "/login");
        }
    }
}
