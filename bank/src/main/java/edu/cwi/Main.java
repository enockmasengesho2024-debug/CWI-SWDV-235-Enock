package edu.cwi;

import java.sql.SQLException;
import java.util.Map;

import org.apache.ibatis.exceptions.PersistenceException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static edu.cwi.AppRole.ADMIN;
import static edu.cwi.AppRole.PUBLIC;
import static edu.cwi.AppRole.USER;
import edu.cwi.data.MyBatisConfig;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.patch;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import io.javalin.http.HttpResponseException;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import io.javalin.rendering.template.JavalinJte;

public class Main {
    public static void main(String[] args) {
        createApp().start(7071);
    }

    public static Javalin createApp() {
        return createApp(MyBatisConfig.fromEnvironment());
    }

    public static Javalin createApp(MyBatisConfig database) {
        UsersController users = new UsersController(database);
        AuthController auth = new AuthController(users);
        TransactionsController transactions =
                new TransactionsController(auth, new LedgerService(database));

        return Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);

            config.jsonMapper(new JavalinJackson(
                    new ObjectMapper().registerModule(new JavaTimeModule()),
                    false
            ));

            config.fileRenderer(new JavalinJte(TemplateEngine.create(
                    new ResourceCodeResolver("jte"), ContentType.Html
            )));

            config.events.serverStopped(database::close);
            config.routes.beforeMatched(auth::checkAccess);

            config.routes.exception(HttpResponseException.class, (e, ctx) ->
                    ctx.status(e.getStatus()).json(Map.of("error", e.getMessage())));

            config.routes.exception(PersistenceException.class, (e, ctx) -> {
                SQLException sql = sqlCause(e);

                if (sql != null && (sql.getErrorCode() == 1062
                        || "23505".equals(sql.getSQLState()))) {
                    ctx.status(409).json(Map.of(
                            "error", "Username or email already exists."
                    ));
                } else if (sql != null && sql.getSQLState() != null
                        && sql.getSQLState().startsWith("23")) {
                    ctx.status(409).json(Map.of(
                            "error", "The change conflicts with database constraints."
                    ));
                } else {
                    System.err.println("Database operation failed. SQLState: "
                            + (sql == null ? "unknown" : sql.getSQLState()));
                    ctx.status(500).json(Map.of("error", "Database operation failed."));
                }
            });

            config.routes.apiBuilder(() -> {
                get("/", ctx -> ctx.redirect("/login"), PUBLIC);
                get("/index.html", ctx -> ctx.redirect("/login"), PUBLIC);
                get("/login", auth::showLogin, PUBLIC);
                post("/login", auth::authenticate, PUBLIC);
                get("/auth/csrf", auth::issueFormToken, PUBLIC);

                get("/session", auth::sessionInfo, USER);
                post("/logout", auth::logout, USER);
                get("/account", transactions::getAll, USER);
                get("/account.html", ctx -> ctx.redirect("/account"), USER);
                
                AccountController accountController = new AccountController(auth, database);

                get("/accounts", accountController::renderAccounts, USER);
                get("/account/{id}", accountController::renderDetail, USER);

                path("/users", () -> {
                    get(users::getAll, ADMIN);
                    post(users::create, ADMIN);

                    path("/{id}", () -> {
                        get(ctx -> users.getOne(ctx, ctx.pathParam("id")), ADMIN);
                        patch(ctx -> users.update(ctx, ctx.pathParam("id")), ADMIN);
                        delete(ctx -> users.delete(ctx, ctx.pathParam("id")), ADMIN);
                    });
                });

                path("/transactions", () -> {
                    get(transactions::getAll, USER);
                    post(transactions::create, USER);

                    path("/{id}", () -> {
                        get(ctx -> transactions.getOne(ctx, ctx.pathParam("id")), USER);
                        patch(ctx -> transactions.update(ctx, ctx.pathParam("id")), USER);
                        delete(ctx -> transactions.delete(ctx, ctx.pathParam("id")), USER);

                        post("/update", ctx ->
                                transactions.update(ctx, ctx.pathParam("id")), USER);
                        post("/delete", ctx ->
                                transactions.delete(ctx, ctx.pathParam("id")), USER);
                    });
                });
                

            });
        });
    }

    private static SQLException sqlCause(Throwable error) {
        while (error != null) {
            if (error instanceof SQLException sql) {
                return sql;
            }
            error = error.getCause();
        }
        return null;
    }
}