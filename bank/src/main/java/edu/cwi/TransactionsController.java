package edu.cwi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.input;
import static j2html.TagCreator.label;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.p;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.title;
import static j2html.TagCreator.tr;
import j2html.tags.DomContent;

public class TransactionsController implements CrudHandler {

   
    private final Map<Integer, TransactionData> transactions =
            new ConcurrentSkipListMap<>();

    private final AtomicInteger nextId = new AtomicInteger(1);

    private record TransactionData(
            int id, String description, BigDecimal amount) {
    }

    @Override
    public void getAll(Context ctx) {
        ctx.html(page("Transactions",
                h2("Add transaction"),
                transactionForm("/transactions", "", ""),
                h2("Saved transactions"),
                p(transactions.isEmpty() ? "No transactions yet." : ""),
                table(
                    thead(tr(
                        th("ID"),
                        th("Description"),
                        th("Amount"),
                        th("Actions")
                        )),
                    tbody(each(transactions.values(), transaction -> tr(
                        td(String.valueOf(transaction.id())),
                        td(transaction.description()),
                        td(transaction.amount().toPlainString()),
                        td(
                            a("View / edit").withHref(
                                    "/transactions/" + transaction.id()
                                        ),
                                        form(
                                            button("Delete").withType("submit")
                                    )
                                .withMethod("post")
                                .withAction(
                                    "/transactions/"
                                    + transaction.id()
                                    + "/delete"
                                    )
                                )
                        )))
                )
        ));
    }

    @Override
    public void getOne(Context ctx, String resourceId) {
        TransactionData transaction = transactions.get(parseId(resourceId));

        if (transaction == null) {
            throw new NotFoundResponse("Transaction not found.");
        }

        ctx.html(page("Transaction " + transaction.id(),
                transactionForm(
                        "/transactions/" + transaction.id() + "/update",
                        transaction.description(),
                        transaction.amount().toPlainString()
                ),
                p(a("Back to transactions").withHref("/transactions"))
        ));
    }

    @Override
    public void create(Context ctx) {
        TransactionData transaction =
                readTransaction(ctx, nextId.getAndIncrement());

        transactions.put(transaction.id(), transaction);

        ctx.redirect("/transactions", HttpStatus.SEE_OTHER);
    }

    @Override
    public void update(Context ctx, String resourceId) {
        int id = parseId(resourceId);
        TransactionData updated = readTransaction(ctx, id);

        if (transactions.replace(id, updated) == null) {
            throw new NotFoundResponse("Transaction not found.");
        }

        ctx.redirect("/transactions", HttpStatus.SEE_OTHER);
    }

    @Override
    public void delete(Context ctx, String resourceId) {
        if (transactions.remove(parseId(resourceId)) == null) {
            throw new NotFoundResponse("Transaction not found.");
        }

        ctx.redirect("/transactions", HttpStatus.SEE_OTHER);
    }

    private int parseId(String resourceId) {
        try {
            int id = Integer.parseInt(resourceId);

            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException ignored) {
        }

        throw new BadRequestResponse(
                "Transaction ID must be a positive integer."
        );
    }

    private TransactionData readTransaction(Context ctx, int id) {
        String description = ctx.formParam("description");
        String amountText = ctx.formParam("amount");

        if (description == null || description.isBlank()
                || description.trim().length() > 120) {
            throw new BadRequestResponse(
                    "Enter a description of 1 to 120 characters."
            );
        }

        try {
            BigDecimal amount = new BigDecimal(
                    amountText == null ? "" : amountText.trim()
            ).setScale(2, RoundingMode.UNNECESSARY);

            return new TransactionData(id, description.trim(), amount);

        } catch (NumberFormatException | ArithmeticException e) {
            throw new BadRequestResponse(
                    "Enter an amount with at most two decimal places."
            );
        }
    }

    private DomContent transactionForm(
            String action, String description, String amount) {

        return form(
            p(
                label("Description").withFor("description"),
                br(),
                input()
                    .withId("description")
                    .withName("description")
                    .withType("text")
                    .withValue(description)
                    .attr("maxlength", "120")
                    .isRequired()
            ),

            p(
                label("Amount").withFor("amount"),
                br(),
                input()
                    .withId("amount")
                    .withName("amount")
                    .withType("number")
                    .withValue(amount)
                    .attr("step", "0.01")
                    .isRequired()
                ),
                button("Save transaction").withType("submit")
        ).withMethod("post").withAction(action);
    }

    private String page(String heading, DomContent... content) {
        return "<!DOCTYPE html>" + html(
                head(
                        meta().attr("charset", "UTF-8"),
                        title(heading)
                ),
                body(
                        h1(heading),
                        div(content)
                )
        ).render();
    }
}

// I used the table  to make the transaction easy to understand.