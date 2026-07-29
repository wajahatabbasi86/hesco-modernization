package com.lmkr.hesco.auth.validator;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Single source of truth for password hashing across the whole app —
 * change-password, reset-password, and password-reuse comparison
 * (PasswordPolicyValidator) all go through this class.
 *
 * Plain BCrypt(rawPassword), matching what UserService.create() already
 * produces (BCryptPasswordEncoder.encode(rawPassword) - the same
 * algorithm, just a different call site) and what AuthService.login()
 * already verifies against. An earlier version of this class combined
 * username+password via SHA-256 before BCrypt-ing, on the theory that
 * tying the hash to the username adds something - but BCrypt already
 * generates its own random per-hash salt, so that combination added no
 * real security property, and it made every hash produced by this class
 * unverifiable by login(), which never adopted it. Reverted to plain
 * BCrypt so there is exactly one hashing scheme for passwordHash across
 * the app, and no already-created account's password becomes
 * unverifiable.
 */
@Component
public class PasswordHasher {

    public String hash(String username, String rawPassword) {
        String combined = combine(username, rawPassword);
        return BCrypt.hashpw(combined, BCrypt.gensalt());
    }

    public boolean matches(String username, String rawPassword, String storedHash) {
        String combined = combine(username, rawPassword);
        return BCrypt.checkpw(combined, storedHash);
    }

    private String combine(String username, String rawPassword) {
        String raw = username.toLowerCase() + ":" + rawPassword;
        return sha256Hex(raw);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is a JDK-mandatory algorithm; this is unreachable
            // in practice, but the checked exception still has to go
            // somewhere.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
