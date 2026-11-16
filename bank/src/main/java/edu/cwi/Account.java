package edu.cwi;

import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public final class Account {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final String username;
    private final String name;
    private final byte[] salt = new byte[16];
    private final byte[] passwordHash;
    private final Map<Integer, Transaction> transactions = new LinkedHashMap<>();
    private int nextId = 1;

    public Account(String username, String name, String password) {
        this.username = username;
        this.name = name;
        RANDOM.nextBytes(salt);
        this.passwordHash = hash(password);
    }

    public String username() {
        return username;
    }

    public boolean passwordMatches(String password) {
        return password != null
                && MessageDigest.isEqual(passwordHash, hash(password));
    }

    private byte[] hash(String password) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 210_000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is unavailable.", e);
        } finally {
            spec.clearPassword();
        }
    }

    public synchronized Transaction findTransaction(int id) {
        return transactions.get(id);
    }

    public synchronized void addTransaction(String description, BigDecimal amount) {
        int id = nextId++;
        transactions.put(id, new Transaction(id, description, amount));
    }

    public synchronized boolean updateTransaction(
            int id, String description, BigDecimal amount) {
        if (!transactions.containsKey(id)) {
            return false;
        }
        transactions.put(id, new Transaction(id, description, amount));
        return true;
    }

    public synchronized boolean deleteTransaction(int id) {
        return transactions.remove(id) != null;
    }

    public synchronized UserAccount view() {
        Map<String, String> details = new LinkedHashMap<>();
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction transaction : transactions.values()) {
            details.put(String.valueOf(transaction.id()),
                    transaction.description() + " ($"
                            + transaction.amount().toPlainString() + ")");
            balance = balance.add(transaction.amount());
        }
        return new UserAccount(name, balance.doubleValue(), details);
    }

    public static Map<String, Account> sampleAccounts() {
        
        Account enock = new Account("enock", "Enock", "Enock123!");
        enock.addTransaction("Salary", new BigDecimal("1500.00"));
        enock.addTransaction("Groceries", new BigDecimal("-249.50"));

        Account alice = new Account("alice", "Alice", "Alice123!");
        alice.addTransaction("Deposit", new BigDecimal("2000.00"));
        alice.addTransaction("Books", new BigDecimal("-85.75"));

        return Map.of(enock.username(), enock, alice.username(), alice);
    }
}
