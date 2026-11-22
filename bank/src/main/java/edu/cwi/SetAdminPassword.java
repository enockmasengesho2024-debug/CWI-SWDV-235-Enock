package edu.cwi;

import java.io.Console;
import java.util.Arrays;

import edu.cwi.data.MyBatisConfig;
import edu.cwi.data.UserMapper;

public class SetAdminPassword {
    public static void main(String[] args) {
        String password = System.getenv("BANK_ADMIN_PASSWORD");
        if (password == null) {
            Console console = System.console();
            if (console == null) {
                throw new IllegalStateException(
                        "Run in a terminal, or set BANK_ADMIN_PASSWORD temporarily.");
            }
            char[] first = console.readPassword("New admin password: ");
            char[] second = console.readPassword("Confirm password: ");
            if (first == null || second == null || !Arrays.equals(first, second)) {
                throw new IllegalArgumentException("Passwords do not match.");
            }
            password = new String(first);
            Arrays.fill(first, '\0');
            Arrays.fill(second, '\0');
        }
        String hash = Passwords.hash(password);
        try (MyBatisConfig database = MyBatisConfig.fromEnvironment()) {
            database.write(session -> {
                UserMapper mapper = session.getMapper(UserMapper.class);
                User admin = mapper.findByUsername("admin");
                if (admin == null || !"ADMIN".equals(admin.getRole())) {
                    throw new IllegalStateException("The SQL script's admin user was not found.");
                }
                admin.setPassword(hash);
                mapper.update(admin);
                return null;
            });
        }
        System.out.println("The admin password hash has been saved.");
    }
}
