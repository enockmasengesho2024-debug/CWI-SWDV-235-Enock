package edu.cwi;

import static j2html.TagCreator.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import j2html.tags.DomContent;

public class TransactionsController implements CrudHandler {
    private final AuthController auth;
    private final LedgerService ledger;

    public TransactionsController(AuthController auth, LedgerService ledger) {
        this.auth = auth;
        this.ledger = ledger;
    }

    @Override
    public void getAll(Context ctx) {
        User user = auth.requireUser(ctx);
        LedgerService.Snapshot snapshot =
                ledger.snapshot(user, ctx.queryParam("accountId"));

        Map<String, Object> model = new LinkedHashMap<>();
        model.put("title", "My Account");
        model.put("account", snapshot.view());
        model.put("accounts", snapshot.accounts());
        model.put("accountId", snapshot.accountId());
        model.put("csrfToken", auth.csrfToken(ctx));

        ctx.render("accountPage.jte", model);
    }

    @Override
    public void getOne(Context ctx, String resourceId) {
        long userId = auth.requireUser(ctx).getId();
        long transactionId = UsersController.id(resourceId);
        Transaction transaction = ledger.find(userId, transactionId);

        ctx.html("<!DOCTYPE html>" + html(
                head(
                        meta().attr("charset", "UTF-8"),
                        title("Edit transaction")
                ),
                body(
                        h1("Edit transaction"),
                        transactionForm(transaction, auth.csrfToken(ctx)),
                        p(a("Back to account").withHref(
                                "/account?accountId=" + transaction.getAccountId()))
                )
        ).render());
    }

    @Override
    public void create(Context ctx) {
        long userId = auth.requireUser(ctx).getId();
        long accountId = UsersController.id(ctx.formParam("accountId"));

        ledger.create(userId, accountId, description(ctx), amount(ctx));

        ctx.redirect("/account?accountId=" + accountId, HttpStatus.SEE_OTHER);
    }

    @Override
    public void update(Context ctx, String resourceId) {
        long userId = auth.requireUser(ctx).getId();
        long transactionId = UsersController.id(resourceId);
        Transaction transaction = ledger.find(userId, transactionId);

        ledger.update(userId, transactionId, description(ctx), amount(ctx));

        ctx.redirect("/account?accountId=" + transaction.getAccountId(),
                HttpStatus.SEE_OTHER);
    }

    @Override
    public void delete(Context ctx, String resourceId) {
        long userId = auth.requireUser(ctx).getId();
        long transactionId = UsersController.id(resourceId);
        Transaction transaction = ledger.find(userId, transactionId);

        ledger.delete(userId, transactionId);

        ctx.redirect("/account?accountId=" + transaction.getAccountId(),
                HttpStatus.SEE_OTHER);
    }

    private String description(Context ctx) {
        String value = ctx.formParam("description");
        if (value == null || value.isBlank() || value.trim().length() > 255) {
            throw new BadRequestResponse("Description must contain 1 to 255 characters.");
        }
        return value.trim();
    }

    private BigDecimal amount(Context ctx) {
        String value = ctx.formParam("amount");
        if (value == null || value.isBlank()) {
            throw new BadRequestResponse("Enter a valid amount.");
        }

        try {
            BigDecimal signedAmount = new BigDecimal(value.trim());
            BigDecimal positive = UsersController.positiveMoney(signedAmount.abs());
            return signedAmount.signum() < 0 ? positive.negate() : positive;
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("Enter a valid amount.");
        }
    }

    private DomContent transactionForm(Transaction transaction, String csrfToken) {
        String description = transaction.getDescription();

        return form(
                input().withType("hidden")
                        .withName("csrfToken")
                        .withValue(csrfToken),
                p(
                        label("Description").withFor("description"),
                        br(),
                        input().withId("description")
                                .withName("description")
                                .withType("text")
                                .withValue(description == null ? "" : description)
                                .attr("maxlength", "255")
                                .isRequired()
                ),
                p(
                        label("Amount").withFor("amount"),
                        br(),
                        input().withId("amount")
                                .withName("amount")
                                .withType("number")
                                .withValue(LedgerService.signed(transaction).toPlainString())
                                .attr("step", "0.01")
                                .isRequired()
                ),
                button("Save transaction").withType("submit")
        ).withMethod("post")
         .withAction("/transactions/" + transaction.getId() + "/update");
    }
}