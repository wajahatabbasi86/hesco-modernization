package com.lmkr.hesco.auth.service;

import com.lmkr.hesco.auth.api.dto.LoginResponse;
import com.lmkr.hesco.auth.exception.InactiveAccountException;
import com.lmkr.hesco.auth.exception.InvalidCredentialsException;
import com.lmkr.hesco.auth.exception.MobileLoginNotAllowedException;
import com.lmkr.hesco.auth.repository.AuthUserRepository;
import com.lmkr.hesco.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final JwtService jwtService;
    @Value("${auth.password.expiry-days:30}")
    private long passwordExpiryDays;

    /**
     * A real BCrypt hash is always exactly 60 chars and starts with $2a$/
     * $2b$/$2y$. Legacy HESCO data (data-users.csv) stores plaintext
     * passwords like "12345" in the same column this code now treats as
     * passwordHash - those values fail this check and fall through to the
     * legacy path below. New accounts created going forward always hash
     * on write, so this pattern only ever matches old, not-yet-migrated
     * rows.
     */
    private static final java.util.regex.Pattern BCRYPT_PATTERN =
            java.util.regex.Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    @Transactional
    public LoginResponse login(String username, String rawPassword) {
        AppUser user = findAppUser(username);

        if (!user.isActive()) {
            throw new InactiveAccountException("This account is inactive");
        }

        // Assumption (flagged, unchanged from the original patch): roles
        // with requiresImei == true are mobile-surveyor roles and
        // authenticate via the mobile app's IMEI-bound flow, not this web
        // login endpoint.
        if (user.getRole() != null && user.getRole().isRequiresImei()) {
            throw new MobileLoginNotAllowedException(
                    "This role authenticates via the mobile app, not the web login");
        }

        String storedHash = user.getPasswordHash();
        if (storedHash == null) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        boolean authenticated;
        if (isBcryptHash(storedHash)) {
            authenticated = BCrypt.checkpw(rawPassword, storedHash);
        } else {
            // Legacy plaintext row (data-users.csv confirms these exist in
            // real production data, e.g. "12345", "Lmkt@123456" - none of
            // which are valid BCrypt hashes). Compare directly, and if it
            // matches, immediately rehash to BCrypt and persist - this
            // migrates every legacy account to a proper hash the first
            // time (and only the first time) its owner successfully logs
            // in, with no separate bulk-migration script and no window
            // where a legacy account is usable without ever producing a
            // real hash for it.
            authenticated = constantTimeEquals(rawPassword, storedHash);
            if (authenticated) {
                user.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
                authUserRepository.save(user);
            }
        }

        if (!authenticated) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String boundType = user.getRole() != null && user.getRole().getAssignedBoundType() != null
                ? user.getRole().getAssignedBoundType().name() : null;
        String roleCode = user.getRole() != null ? user.getRole().getCode() : null;

        Long circleId = user.getCircle() != null ? user.getCircle().getId() : null;
        Long divisionId = user.getDivision() != null ? user.getDivision().getId() : null;
        Long subDivisionId = user.getSubDivision() != null ? user.getSubDivision().getId() : null;

        JwtService.IssuedToken issued = jwtService.issue(
                user.getUsername(), user.getId(), roleCode, boundType, circleId, divisionId, subDivisionId);

        return new LoginResponse(
                issued.token(), issued.expiresAt(),
                user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(),
                roleCode, boundType, circleId, divisionId, subDivisionId, user.isPasswordExpired(), user.getPasswordExpiringInDays(), user.isMustChangePassword());
    }

    private boolean isBcryptHash(String value) {
        return BCRYPT_PATTERN.matcher(value).matches();
    }

    /**
     * Plain String.equals() on a password comparison is a timing-attack
     * surface (return time leaks how many leading characters matched).
     * Legacy plaintext passwords in this dataset are already about as
     * weak as it gets, but there's no reason to add an *additional*,
     * easily-avoidable side channel on top of that while they're
     * mid-migration to real hashes.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] x = a.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] y = b.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int diff = x.length ^ y.length;
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            byte bx = i < x.length ? x[i] : 0;
            byte by = i < y.length ? y[i] : 0;
            diff |= bx ^ by;
        }
        return diff == 0;
    }


    private AppUser findAppUser(String username) {

        AppUser user = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        //  Active check
        if (!user.isActive()) {
            throw new InactiveAccountException("This account is inactive");
        }

        //  Mobile role restriction
        if (user.getRole() != null && user.getRole().isRequiresImei()) {
            throw new MobileLoginNotAllowedException(
                    "This role authenticates via the mobile app, not the web login");
        }

        //  Password expiry handling
        boolean passwordExpired = isPasswordExpired(user);

        if (passwordExpired) {
            user.setMustChangePassword(true);
        }

        long passwordExpiredDays =  getPasswordExpiringInDays(user);

        user.setPasswordExpiringInDays(passwordExpiredDays);

        return user;
    }

    private boolean isPasswordExpired(AppUser user) {
        if (user.getPasswordChangedAt() == null) {
            return false; // legacy users
        }

        return OffsetDateTime.now()
                .isAfter(user.getPasswordChangedAt().plusDays(passwordExpiryDays));
    }

    private long getPasswordExpiringInDays(AppUser user) {

        //  Handle null (legacy users)
        if (user.getPasswordChangedAt() == null) {
            return Long.MAX_VALUE; // or -1 based on your API contract
        }

        java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
        java.time.OffsetDateTime expiry =
                user.getPasswordChangedAt().plusDays(passwordExpiryDays);

        //  If already expired
        if (now.isAfter(expiry)) {
            return 0;
        }

        //  Duration-based calculation
        long days = java.time.Duration.between(now, expiry).toDays();

        return Math.max(days, 0);
    }
}