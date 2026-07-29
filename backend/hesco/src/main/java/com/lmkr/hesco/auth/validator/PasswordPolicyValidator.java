package com.lmkr.hesco.auth.validator;

import com.lmkr.hesco.auth.exception.PasswordPolicyViolationException;
import com.lmkr.hesco.auth.exception.PasswordReuseException;
import com.lmkr.hesco.auth.entity.PasswordHistory;
import com.lmkr.hesco.auth.repository.PasswordHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Enforces auth_policy_prompt.md section 1: min 8 chars, upper/lower/
 * digit/special all required, username must not appear inside the
 * password, and no reuse of the last 5 passwords (checked against
 * password_history, which stores hashes - so reuse detection re-hashes
 * the candidate with the SAME PasswordHasher and compares against each
 * stored hash, since hashes can't be reversed or compared any other way).
 */
@Component
public class PasswordPolicyValidator {

    private static final int MIN_LENGTH = 8;
    private static final int HISTORY_DEPTH = 5;

    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordHasher passwordHasher;

    public PasswordPolicyValidator(PasswordHistoryRepository passwordHistoryRepository,
                                   PasswordHasher passwordHasher) {
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Complexity + username-substring checks only - does not touch the
     * database. Call this on every password set (including the very
     * first one for a brand-new account), regardless of whether reuse
     * history exists yet.
     */
    public void validateComplexity(String username, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            throw new PasswordPolicyViolationException(
                    "Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (!UPPER.matcher(rawPassword).find()) {
            throw new PasswordPolicyViolationException("Password must contain at least one uppercase letter");
        }
        if (!LOWER.matcher(rawPassword).find()) {
            throw new PasswordPolicyViolationException("Password must contain at least one lowercase letter");
        }
        if (!DIGIT.matcher(rawPassword).find()) {
            throw new PasswordPolicyViolationException("Password must contain at least one digit");
        }
        if (!SPECIAL.matcher(rawPassword).find()) {
            throw new PasswordPolicyViolationException("Password must contain at least one special character");
        }
        if (username != null && rawPassword.toLowerCase().contains(username.toLowerCase())) {
            throw new PasswordPolicyViolationException("Password must not contain the username");
        }
    }

    /**
     * Reuse check against the last HISTORY_DEPTH stored hashes for this
     * user. Must run AFTER validateComplexity() and BEFORE the new
     * password is actually persisted - the caller is responsible for
     * ordering (see AuthService.changePassword()).
     */
    public void validateNotReused(Long userId, String username, String rawPassword) {
        var recent = passwordHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, HISTORY_DEPTH));

        for (PasswordHistory entry : recent) {
            if (passwordHasher.matches(username, rawPassword, entry.getPasswordHash())) {
                throw new PasswordReuseException(
                        "Password must not match any of your last " + HISTORY_DEPTH + " passwords");
            }
        }
    }
}