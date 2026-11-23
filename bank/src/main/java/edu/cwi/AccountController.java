package edu.cwi;

import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import edu.cwi.data.AccountMapper;
import edu.cwi.data.MyBatisConfig;
import edu.cwi.data.TransactionMapper;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

public class AccountController {
    private final AuthController auth;
    private final MyBatisConfig database;

    public AccountController(AuthController auth, MyBatisConfig database) {
        this.auth = auth;
        this.database = database;
    }

    public void renderAccounts(Context ctx) {
        User user = auth.requireUser(ctx);

        try (SqlSession session = database.openSession()) {
            AccountMapper mapper = session.getMapper(AccountMapper.class);

            ctx.render("accountsPage.jte", Map.of(
                    "name", user.getFirstName() + " " + user.getLastName(),
                    "accounts", mapper.findByUserId(user.getId()),
                    "csrfToken", auth.csrfToken(ctx)
            ));
        }
    }

    public void renderDetail(Context ctx) {
    User user = auth.requireUser(ctx);
    long accountId = UsersController.id(ctx.pathParam("id"));

    try (SqlSession session = database.openSession()) {
        AccountMapper accounts = session.getMapper(AccountMapper.class);
        TransactionMapper transactions = session.getMapper(TransactionMapper.class);

        for (Account account : accounts.findByUserId(user.getId())) {
            if (account.getId() == accountId) {
                ctx.render("accountDetail.jte", Map.of(
                        "name", user.getFirstName() + " " + user.getLastName(),
                        "account", account,
                        "transactions", transactions.findByAccountId(accountId),
                        "csrfToken", auth.csrfToken(ctx)
                    ));
                    return;
                }
            }

            throw new NotFoundResponse("Account not found.");
        }
    }
}