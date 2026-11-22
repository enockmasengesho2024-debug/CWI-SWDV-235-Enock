package edu.cwi;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import edu.cwi.data.AccountMapper;
import edu.cwi.data.MyBatisConfig;
import edu.cwi.data.TransactionMapper;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ConflictResponse;
import io.javalin.http.NotFoundResponse;

public final class LedgerService {
    private final MyBatisConfig database;

    public LedgerService(MyBatisConfig database) {
        this.database = database;
    }

    public record Snapshot(UserAccount view, List<Account> accounts, Long accountId) {}

    public Snapshot snapshot(User user, String selectedId) {
        try (SqlSession session = database.openSession()) {
            List<Account> accounts = session.getMapper(AccountMapper.class).findByUserId(user.getId());
            String name = user.getFirstName() + " " + user.getLastName();
            if (accounts.isEmpty() && selectedId == null) {
                return new Snapshot(new UserAccount(name, 0.0, Map.of()), accounts, null);
            }
            long selected = selectedId == null ? accounts.get(0).getId() : UsersController.id(selectedId);
            Account account = accounts.stream().filter(a -> a.getId() == selected).findFirst()
                    .orElseThrow(() -> new NotFoundResponse("Account not found."));
            Map<String, String> details = new LinkedHashMap<>();
            for (Transaction t : session.getMapper(TransactionMapper.class).findByAccountId(selected)) {
                details.put(t.getId().toString(), t.getTransactionType() + " — "
                        + (t.getDescription() == null ? "" : t.getDescription())
                        + " ($" + t.getAmount().toPlainString() + ")");
            }
            return new Snapshot(new UserAccount(name, account.getBalance().doubleValue(), details),
                    accounts, account.getId());
        }
    }

    public Transaction find(long userId, long id) {
        try (SqlSession session = database.openSession()) {
            Transaction t = session.getMapper(TransactionMapper.class).findById(id);
            if (t == null || session.getMapper(AccountMapper.class).findByUserId(userId)
                    .stream().noneMatch(a -> a.getId().equals(t.getAccountId()))) {
                throw new NotFoundResponse("Transaction not found.");
            }
            return t;
        }
    }

    public void create(long userId, long accountId, String description, BigDecimal signedAmount) {
        BigDecimal amount = UsersController.positiveMoney(signedAmount.abs());
        database.write(session -> {
            Account account = ownedLockedAccount(session, userId, accountId);
            BigDecimal balance = checkedBalance(account.getBalance().add(signedAmount));
            Transaction t = new Transaction();
            t.setAccountId(accountId);
            t.setTransactionType(signedAmount.signum() > 0 ? "DEPOSIT" : "WITHDRAWAL");
            t.setAmount(amount);
            t.setDescription(description);
            session.getMapper(TransactionMapper.class).insert(t);
            account.setBalance(balance);
            session.getMapper(AccountMapper.class).updateBalance(account);
            return null;
        });
    }

    public void update(long userId, long id, String description, BigDecimal signedAmount) {
        BigDecimal amount = UsersController.positiveMoney(signedAmount.abs());
        database.write(session -> {
            TransactionMapper mapper = session.getMapper(TransactionMapper.class);
            Transaction before = mapper.findById(id);
            if (before == null) throw new NotFoundResponse("Transaction not found.");
            Account account = ownedLockedAccount(session, userId, before.getAccountId());
            Transaction t = mapper.lockById(id);
            if (t == null) throw new NotFoundResponse("Transaction not found.");
            editable(t);
            BigDecimal balance = checkedBalance(
                    account.getBalance().subtract(signed(t)).add(signedAmount));
            t.setDescription(description);
            t.setAmount(amount);
            t.setTransactionType(signedAmount.signum() > 0 ? "DEPOSIT" : "WITHDRAWAL");
            mapper.update(t);
            account.setBalance(balance);
            session.getMapper(AccountMapper.class).updateBalance(account);
            return null;
        });
    }

    public void delete(long userId, long id) {
        database.write(session -> {
            TransactionMapper mapper = session.getMapper(TransactionMapper.class);
            Transaction before = mapper.findById(id);
            if (before == null) throw new NotFoundResponse("Transaction not found.");
            Account account = ownedLockedAccount(session, userId, before.getAccountId());
            Transaction t = mapper.lockById(id);
            if (t == null) throw new NotFoundResponse("Transaction not found.");
            editable(t);
            account.setBalance(checkedBalance(account.getBalance().subtract(signed(t))));
            mapper.delete(id);
            session.getMapper(AccountMapper.class).updateBalance(account);
            return null;
        });
    }

    private Account ownedLockedAccount(SqlSession session, long userId, long accountId) {
        Account account = session.getMapper(AccountMapper.class).lockById(accountId);
        if (account == null || account.getUserId() != userId) {
            throw new NotFoundResponse("Account not found.");
        }
        return account;
    }

    public static BigDecimal signed(Transaction t) {
        return t.getTransactionType().equals("DEPOSIT") || t.getTransactionType().equals("TRANSFER_IN")
                ? t.getAmount() : t.getAmount().negate();
    }

    private static void editable(Transaction t) {
        if (t.getTransactionType().startsWith("TRANSFER")) {
            throw new ConflictResponse("Transfers cannot be edited independently.");
        }
    }

    private static BigDecimal checkedBalance(BigDecimal amount) {
        if (amount.signum() < 0) throw new BadRequestResponse("Insufficient balance.");
        if (amount.precision() > 15) throw new BadRequestResponse("Balance exceeds DECIMAL(15,2).");
        return amount;
    }
}

