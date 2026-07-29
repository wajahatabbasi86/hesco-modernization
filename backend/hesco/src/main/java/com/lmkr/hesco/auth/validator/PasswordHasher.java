package com.lmkr.hesco.auth.validator;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hashes username+password together, per requirement. BCrypt silently
 * truncates any input over 72 bytes - simply concatenating a long
 * username with a password risks losing the tail of the password to
 * that truncation with no error raised. To avoid that, the combined
 * string is first run through SHA-256 (always a fixed 64-char hex
 * digest, well under the 72-byte limit) and BCrypt hashes THAT digest,
 * not the raw concatenation. The username is lowercased before hashing
 * so case differences in how it's typed elsewhere never produce a
 * different hash.
 *
 * This ties every stored hash to the username at the time of hashing -
 * AppUser.username is already treated as non-editable post-creation
 * elsewhere in this codebase (SRS §3.2.5), so that coupling holds as
 * long as that rule isn't relaxed later.
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
