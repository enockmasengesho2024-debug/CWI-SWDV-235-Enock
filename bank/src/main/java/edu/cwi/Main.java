package edu.cwi;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import static io.javalin.apibuilder.ApiBuilder.*;

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