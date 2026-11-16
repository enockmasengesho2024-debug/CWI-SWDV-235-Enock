package edu.cwi;

import static j2html.TagCreator.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import j2html.tags.DomContent;

public class TransactionsController implements CrudHandler {
    private final AuthController auth;

    public TransactionsController(AuthController auth) {
        this.auth = auth;
    }

    @Override
    public void getAll(Context ctx) {
       
        UserAccount account = auth.requireAccount(ctx).view();
        ctx.render("accountPage.jte", Map.of(
                "title", "My Account",
                "account", account,
                "csrfToken", auth.csrfToken(ctx)
        ));
    }

    @Override
    public void getOne(Context ctx, String resourceId) {
        Transaction transaction = auth.requireAccount(ctx)
                .findTransaction(parseId(resourceId));
        if (transaction == null) {
            throw new NotFoundResponse("Transaction not found.");
        }
        ctx.html(page("Transaction " + transaction.id(),
                transactionForm(
                        "/transactions/" + transaction.id() + "/update",
                        transaction.description(),
                        transaction.amount().toPlainString(),
                        auth.csrfToken(ctx)
                ),
                p(a("Back to account").withHref("/account"))
        ));
    }

    @Override
    public void create(Context ctx) {
        Account account = auth.requireAccount(ctx);
        Transaction input = readTransaction(ctx, 0);
        account.addTransaction(input.description(), input.amount());
        ctx.redirect("/account", HttpStatus.SEE_OTHER);
    }

    @Override
    public void update(Context ctx, String resourceId) {
        Account account = auth.requireAccount(ctx);
        int id = parseId(resourceId);
        Transaction input = readTransaction(ctx, id);
        if (!account.updateTransaction(id, input.description(), input.amount())) {
            throw new NotFoundResponse("Transaction not found.");
        }
        ctx.redirect("/account", HttpStatus.SEE_OTHER);
    }

    @Override
    public void delete(Context ctx, String resourceId) {
        if (!auth.requireAccount(ctx).deleteTransaction(parseId(resourceId))) {
            throw new NotFoundResponse("Transaction not found.");
        }
        ctx.redirect("/account", HttpStatus.SEE_OTHER);
    }

    private int parseId(String resourceId) {
        try {
            int id = Integer.parseInt(resourceId);
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException ignored) {
        }
        throw new BadRequestResponse("Transaction ID must be a positive integer.");
    }

    private Transaction readTransaction(Context ctx, int id) {
        String description = ctx.formParam("description");
        String amountText = ctx.formParam("amount");
        if (description == null || description.isBlank()
                || description.trim().length() > 120) {
            throw new BadRequestResponse("Enter a description of 1 to 120 characters.");
        }
        try {
            BigDecimal amount = new BigDecimal(
                    amountText == null ? "" : amountText.trim()
            ).setScale(2, RoundingMode.UNNECESSARY);
            return new Transaction(id, description.trim(), amount);
        } catch (NumberFormatException | ArithmeticException e) {
            throw new BadRequestResponse(
                    "Enter an amount with at most two decimal places.");
        }
    }

    private DomContent transactionForm(
            String action, String description, String amount, String csrfToken) {
        return form(
                input().withType("hidden").withName("csrfToken")
                        .withValue(csrfToken),
                p(
                        label("Description").withFor("description"), br(),
                        input().withId("description").withName("description")
                                .withType("text").withValue(description)
                                .attr("maxlength", "120").isRequired()
                ),
                p(
                        label("Amount").withFor("amount"), br(),
                        input().withId("amount").withName("amount")
                                .withType("number").withValue(amount)
                                .attr("step", "0.01").isRequired()
                ),
                button("Save transaction").withType("submit")
        ).withMethod("post").withAction(action);
    }

    private String page(String heading, DomContent... content) {
        return "<!DOCTYPE html>" + html(
                head(meta().attr("charset", "UTF-8"), title(heading)),
                body(h1(heading), div(content))
        ).render();
    }
}

