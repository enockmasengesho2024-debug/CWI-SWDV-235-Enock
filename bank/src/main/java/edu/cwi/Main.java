package edu.cwi;


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
        TransactionsController controller = new TransactionsController();

        Javalin.create(config -> {

            config.staticFiles.add("/public", Location.CLASSPATH);

            config.routes.post("/account", ctx -> {
                ctx.redirect("/account.html");
            });

            config.routes.get("/logout", ctx -> {
                ctx.redirect("/");
            });

            config.fileRenderer(new JavalinJte (
                TemplateEngine.create(
                    new ResourceCodeResolver("jte"), ContentType.Html
                )
            ));

            config.routes.apiBuilder(() -> {
                path("/transactions", () -> {

                    get(controller::getAll);
                    post(controller::create);

                    path("/{id}", () -> {

                        get(ctx ->
                                controller.getOne(ctx, ctx.pathParam("id"))
                        );

                        patch(ctx ->
                                controller.update(ctx, ctx.pathParam("id"))
                        );

                        delete(ctx ->
                                controller.delete(ctx, ctx.pathParam("id"))
                        );

                        post("/update", ctx ->
                                controller.update(ctx, ctx.pathParam("id"))
                        );

                        post("/delete", ctx ->
                                controller.delete(ctx, ctx.pathParam("id"))
                        );
                    });
                });
            });

        }).start(7071);
    }
}