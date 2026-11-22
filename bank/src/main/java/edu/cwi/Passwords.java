package edu.cwi;

import java.nio.charset.StandardCharsets;

import org.mindrot.jbcrypt.BCrypt;

import io.javalin.http.BadRequestResponse;

public final class Passwords {
    private Passwords() {}

    public static String hash(String password) {
        if (password == null || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadRequestResponse("Password must have at least 8 characters and at most 72 UTF-8 bytes.");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static boolean matches(String password, String hash) {
        if (password == null || hash == null
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
        
            return false;
        }
    }
}
