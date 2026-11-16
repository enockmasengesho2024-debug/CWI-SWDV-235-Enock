package edu.cwi;

import static edu.cwi.AppRole.PUBLIC;
import static edu.cwi.AppRole.USER;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.patch;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;

public class Main {
    public static void main(String[] args) {
        createApp().start(7071);
    }

    public static Javalin createApp() {
        AuthController auth = new AuthController();
        TransactionsController transactions = new TransactionsController(auth);

        return Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.fileRenderer(new JavalinJte(
                    TemplateEngine.create(
                            new ResourceCodeResolver("jte"), ContentType.Html)
            ));

            config.routes.beforeMatched(auth::checkAccess);

            config.routes.apiBuilder(() -> {
                get("/", ctx -> ctx.redirect("/login"), PUBLIC);
                get("/index.html", ctx -> ctx.redirect("/login"), PUBLIC);
                get("/login", auth::showLogin, PUBLIC);
                post("/login", auth::login, PUBLIC);
                post("/logout", auth::logout, USER);

                get("/account", transactions::getAll, USER);
                get("/account.html", ctx -> ctx.redirect("/account"), USER);

                path("/transactions", () -> {
                    get(transactions::getAll, USER);
                    post(transactions::create, USER);
                    path("/{id}", () -> {
                        get(ctx -> transactions.getOne(
                                ctx, ctx.pathParam("id")), USER);
                        patch(ctx -> transactions.update(
                                ctx, ctx.pathParam("id")), USER);
                        delete(ctx -> transactions.delete(
                                ctx, ctx.pathParam("id")), USER);

                        post("/update", ctx -> transactions.update(
                                ctx, ctx.pathParam("id")), USER);
                        post("/delete", ctx -> transactions.delete(
                                ctx, ctx.pathParam("id")), USER);
                    });
                });
            });
        });
    }
}
