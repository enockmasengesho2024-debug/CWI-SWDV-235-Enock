package edu.cwi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.javalin.http.Context;
import io.javalin.http.Cookie;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpStatus;
import io.javalin.http.SameSite;
import io.javalin.http.UnauthorizedResponse;

public final class AuthController {
    private static final String COOKIE_NAME = "cwi_session";
    private static final int SESSION_SECONDS = 30 * 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsersController users;
    private final Map<String, LoginSession> sessions = new ConcurrentHashMap<>();

    private record LoginSession(
            Long userId,
            String passwordHash,
            String csrfToken,
            Instant expiresAt
    ) {}

    public AuthController(UsersController users) {
        this.users = users;
    }

    public void showLogin(Context ctx) {
        ctx.header("Cache-Control", "no-store");
        LoginSession session = findSession(ctx);

        if (sessionUser(session) != null) {
            ctx.redirect("/account");
            return;
        }

        if (session == null) {
            session = createSession(ctx, null);
        }

        renderLogin(ctx, session, "", "");
    }

    public void authenticate(Context ctx) {
        ctx.header("Cache-Control", "no-store");
        LoginSession session = findSession(ctx);
        verifyFormToken(ctx, session);

        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        User user = users.authenticateUser(ctx, username, password);

        if (user == null) {
            ctx.status(HttpStatus.UNAUTHORIZED);
            renderLogin(ctx, session, "Incorrect username or password.",
                    username == null ? "" : username);
            return;
        }

        discardSession(ctx);
        LoginSession authenticated = createSession(ctx, user);

        if ("json".equals(ctx.queryParam("return"))) {
            ctx.json(Map.of(
                    "user", user.response(),
                    "csrfToken", authenticated.csrfToken()
            ));
        } else {
            ctx.redirect("/account", HttpStatus.SEE_OTHER);
        }
    }

    public void issueFormToken(Context ctx) {
        ctx.header("Cache-Control", "no-store");
        LoginSession session = findSession(ctx);

        if (session == null) {
            session = createSession(ctx, null);
        }

        ctx.json(Map.of("csrfToken", session.csrfToken()));
    }

    public void sessionInfo(Context ctx) {
        ctx.json(Map.of(
                "user", requireUser(ctx).response(),
                "csrfToken", csrfToken(ctx)
        ));
    }

    public void logout(Context ctx) {
        discardSession(ctx);
        writeCookie(ctx, "", 0);
        ctx.redirect("/login", HttpStatus.SEE_OTHER);
    }

    public void checkAccess(Context ctx) {
        if (!ctx.routeRoles().contains(AppRole.USER)
                && !ctx.routeRoles().contains(AppRole.ADMIN)) {
            return;
        }

        ctx.header("Cache-Control", "no-store");
        LoginSession session = findSession(ctx);
        User user = sessionUser(session);

        if (user == null) {
            if (ctx.path().startsWith("/users") || ctx.path().equals("/session")) {
                throw new UnauthorizedResponse("Please log in.");
            }

            ctx.redirect("/login", HttpStatus.SEE_OTHER);
            ctx.skipRemainingHandlers();
            return;
        }

        ctx.attribute("currentUser", user);

        if (ctx.routeRoles().contains(AppRole.ADMIN)
                && !"ADMIN".equals(user.getRole())) {
            throw new ForbiddenResponse("Administrator access is required.");
        }

        String method = ctx.method().name();
        if (!method.equals("GET") && !method.equals("HEAD")
                && !method.equals("OPTIONS")) {
            verifyFormToken(ctx, session);
        }
    }

    public User requireUser(Context ctx) {
        User user = ctx.attribute("currentUser");

        if (user == null) {
            user = sessionUser(findSession(ctx));
        }

        if (user == null) {
            throw new UnauthorizedResponse("Please log in.");
        }

        return user;
    }

    public String csrfToken(Context ctx) {
        LoginSession session = findSession(ctx);

        if (session == null) {
            throw new UnauthorizedResponse("Please log in.");
        }

        return session.csrfToken();
    }

    private User sessionUser(LoginSession session) {
        if (session == null || session.userId() == null) {
            return null;
        }

        User user = users.findUser(session.userId());
        if (user == null || !user.getPassword().equals(session.passwordHash())) {
            return null;
        }

        return user;
    }

    private void renderLogin(
            Context ctx, LoginSession session, String error, String username) {
        ctx.render("loginPage.jte", Map.of(
                "error", error,
                "username", username,
                "csrfToken", session.csrfToken()
        ));
    }

    private LoginSession createSession(Context ctx, User user) {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry ->
                !now.isBefore(entry.getValue().expiresAt()));

        String sessionId = randomToken();
        LoginSession session = new LoginSession(
                user == null ? null : user.getId(),
                user == null ? null : user.getPassword(),
                randomToken(),
                now.plusSeconds(SESSION_SECONDS)
        );

        sessions.put(sessionId, session);
        writeCookie(ctx, sessionId, SESSION_SECONDS);
        return session;
    }

    private LoginSession findSession(Context ctx) {
        String sessionId = ctx.cookie(COOKIE_NAME);
        if (sessionId == null) {
            return null;
        }

        LoginSession session = sessions.get(sessionId);
        if (session != null && !Instant.now().isBefore(session.expiresAt())) {
            sessions.remove(sessionId);
            return null;
        }

        return session;
    }

    private void discardSession(Context ctx) {
        String sessionId = ctx.cookie(COOKIE_NAME);
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
    }

    private void writeCookie(Context ctx, String value, int maxAge) {
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        cookie.setSameSite(SameSite.LAX);
        cookie.setSecure(ctx.req().isSecure());
        ctx.cookie(cookie);
    }

    private void verifyFormToken(Context ctx, LoginSession session) {
        String supplied = ctx.header("X-CSRF-Token");
        if (supplied == null) {
            supplied = ctx.formParam("csrfToken");
        }

        if (session == null || supplied == null
                || !MessageDigest.isEqual(
                        session.csrfToken().getBytes(StandardCharsets.UTF_8),
                        supplied.getBytes(StandardCharsets.UTF_8))) {
            throw new ForbiddenResponse(
                    "Invalid or expired form. Reload the page and try again.");
        }
    }

    private static String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}