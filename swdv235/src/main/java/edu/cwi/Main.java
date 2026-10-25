package edu.cwi;

import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {

        Javalin.create(config -> {

            config.routes.get("/hello/{name}", ctx -> {
                String name = ctx.pathParam("name");

                ctx.result("Hello, " + name);
            });

            config.routes.get("/hello/{name}/html", ctx -> {
                String name = ctx.pathParam("name");

                String safeName = name
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;");

                String html = """
                        <html>
                            <head>
                                <title>Javalin Intro</title>
                            </head>
                            <body>
                                <h1>Hello, {name}</h1>
                            </body>
                        </html>
                        """;

                ctx.html(html.replace("{name}", safeName));
            });

        }).start(7070);
    }
}