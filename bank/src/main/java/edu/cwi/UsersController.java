package edu.cwi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;

import edu.cwi.data.AccountMapper;
import edu.cwi.data.MyBatisConfig;
import edu.cwi.data.TransactionMapper;
import edu.cwi.data.UserMapper;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

public final class UsersController implements CrudHandler {
    private final MyBatisConfig database;

    public UsersController(MyBatisConfig database) {
        this.database = database;
    }

    public record UserRequest(String username, String password, String email,
                              String firstName, String lastName, String role,
                              BigDecimal openingDeposit) {}

    @Override
    public void getAll(Context ctx) {
        try (SqlSession session = database.openSession()) {
            ctx.json(session.getMapper(UserMapper.class).findAll()
                    .stream().map(User::response).toList());
        }
    }

    @Override
    public void getOne(Context ctx, String resourceId) {
        ctx.json(requiredUser(id(resourceId)).response());
    }

    @Override
    public void create(Context ctx) {
        User user = ctx.bodyAsClass(User.class);
        BigDecimal deposit = new BigDecimal("5.00");

        user.setId(null);
        user.setUsername(text(user.getUsername(), "username", 50));
        user.setEmail(email(user.getEmail()));
        user.setFirstName(text(user.getFirstName(), "firstName", 100));
        user.setLastName(text(user.getLastName(), "lastName", 100));
        user.setRole(role(user.getRole() == null ? "CUSTOMER" : user.getRole()));
        user.setPassword(Passwords.hash(user.getPassword()));

        try (SqlSession session = database.getSqlSessionFactory().openSession()) {
            UserMapper userMapper = session.getMapper(UserMapper.class);
            AccountMapper accountMapper = session.getMapper(AccountMapper.class);
            TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);

            try {
                userMapper.insert(user);

                Account account = new Account();
                account.setUserId(user.getId());
                account.setAccountType("CHECKING");
                account.setBalance(deposit);
                accountMapper.insert(account);

                Transaction transaction = new Transaction();
                transaction.setAccountId(account.getId());
                transaction.setTransactionType("DEPOSIT");
                transaction.setAmount(deposit);
                transaction.setDescription("Opening deposit");
                transactionMapper.insert(transaction);

                session.commit();
            } catch (RuntimeException ex) {
                session.rollback();
                throw ex;
            }
        }

        ctx.status(201)
            .header("Location", "/users/" + user.getId())
            .json(user.response());
    }
    @Override
    public void update(Context ctx, String resourceId) {
        long id = id(resourceId);
        UserRequest request = ctx.bodyAsClass(UserRequest.class);
        if (request.openingDeposit() != null) {
            throw new BadRequestResponse("openingDeposit is only allowed when creating a user.");
        }
        User actor = ctx.attribute("currentUser");
        User updated = database.write(session -> {
            UserMapper mapper = session.getMapper(UserMapper.class);
            User user = mapper.lockById(id);
            if (user == null) throw new NotFoundResponse("User not found.");
            if (request.username() != null) user.setUsername(text(request.username(), "username", 50));
            if (request.email() != null) user.setEmail(email(request.email()));
            if (request.firstName() != null) user.setFirstName(text(request.firstName(), "firstName", 100));
            if (request.lastName() != null) user.setLastName(text(request.lastName(), "lastName", 100));
            if (request.password() != null) user.setPassword(Passwords.hash(request.password()));
            if (request.role() != null) {
                String newRole = role(request.role());
                if (actor != null && actor.getId() == id && !newRole.equals("ADMIN")) {
                    throw new ConflictResponse("You cannot remove your own administrator role.");
                }
                user.setRole(newRole);
            }
            mapper.update(user);
            return user;
        });
        ctx.json(updated.response());
    }

    @Override
    public void delete(Context ctx, String resourceId) {
        long id = id(resourceId);
        User actor = ctx.attribute("currentUser");
        if (actor != null && actor.getId() == id) {
            throw new ConflictResponse("You cannot delete your own administrator account.");
        }
        database.write(session -> {
            if (session.getMapper(UserMapper.class).lockById(id) == null) {
                throw new NotFoundResponse("User not found.");
            }

            session.getMapper(TransactionMapper.class).deleteByUserId(id);
            session.getMapper(AccountMapper.class).deleteByUserId(id);
            session.getMapper(UserMapper.class).delete(id);
            return null;
        });
        ctx.json(Map.of("deleted", true, "id", id));
    }

    public User authenticateUser(Context ctx, String username, String password) {
        if (username == null) return null;
        try (SqlSession session = database.openSession()) {
            User user = session.getMapper(UserMapper.class).findByUsername(username.trim());
            return user != null && Passwords.matches(password, user.getPassword()) ? user : null;
        }
    }

    public User findUser(long id) {
        try (SqlSession session = database.openSession()) {
            return session.getMapper(UserMapper.class).findById(id);
        }
    }

    private User requiredUser(long id) {
        User user = findUser(id);
        if (user == null) throw new NotFoundResponse("User not found.");
        return user;
    }

    public static long id(String value) {
        try {
            long id = Long.parseLong(value);
            if (id > 0) return id;
        } catch (NumberFormatException ignored) {}
        throw new BadRequestResponse("ID must be a positive integer.");
    }

    public static BigDecimal positiveMoney(BigDecimal value) {
        if (value == null) throw new BadRequestResponse("openingDeposit must be a positive amount.");
        try {
            value = value.setScale(2, RoundingMode.UNNECESSARY);
            if (value.signum() <= 0 || value.precision() > 15) {
                throw new ArithmeticException();
            }
            return value;
        } catch (ArithmeticException e) {
            throw new BadRequestResponse("Amount must be positive, fit DECIMAL(15,2), and have at most two decimal places.");
        }
    }

    private static String text(String value, String field, int maximum) {
        if (value == null || value.isBlank() || value.trim().length() > maximum) {
            throw new BadRequestResponse(field + " is required and must be at most " + maximum + " characters.");
        }
        return value.trim();
    }

    private static String email(String value) {
        value = text(value, "email", 255);
        if (!value.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new BadRequestResponse("Enter a valid email address.");
        }
        return value;
    }

    private static String role(String value) {
        if (!Set.of("CUSTOMER", "TELLER", "ADMIN").contains(value)) {
            throw new BadRequestResponse("Role must be CUSTOMER, TELLER, or ADMIN.");
        }
        return value;
    }
}
