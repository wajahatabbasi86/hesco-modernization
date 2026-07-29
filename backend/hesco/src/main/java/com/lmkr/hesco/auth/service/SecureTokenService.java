package com.lmkr.hesco.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/**
 * auth_policy_prompt.md §4: "generate secure random token" + "store
 * hashed token". The raw token is only ever returned once (in the
 * forgot-password response) and never persisted - only its SHA-256 hex
 * digest is stored, so a DB read alone can never be used to reset a
 * password.
 */
@Component
public class SecureTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32; // 256 bits

    public String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is a JDK-mandatory algorithm; unreachable in
            // practice, but the checked exception has to go somewhere.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
