package com.lmkr.hesco.auth.service;

import com.lmkr.hesco.auth.api.dto.ForgotPasswordResponse;
import com.lmkr.hesco.auth.api.dto.LoginHistoryEntryResponse;
import com.lmkr.hesco.auth.api.dto.LoginResponse;
import com.lmkr.hesco.auth.entity.LoginHistory;
import com.lmkr.hesco.auth.entity.PasswordChangeAudit;
import com.lmkr.hesco.auth.entity.PasswordHistory;
import com.lmkr.hesco.auth.entity.PasswordResetToken;
import com.lmkr.hesco.auth.exception.InactiveAccountException;
import com.lmkr.hesco.auth.exception.InvalidCredentialsException;
import com.lmkr.hesco.auth.exception.InvalidResetTokenException;
import com.lmkr.hesco.auth.exception.MobileLoginNotAllowedException;
import com.lmkr.hesco.auth.exception.PasswordReuseException;
import com.lmkr.hesco.auth.exception.RateLimitExceededException;
import com.lmkr.hesco.auth.repository.AuthUserRepository;
import com.lmkr.hesco.auth.repository.LoginHistoryRepository;
import com.lmkr.hesco.auth.repository.PasswordChangeAuditRepository;
import com.lmkr.hesco.auth.repository.PasswordHistoryRepository;
import com.lmkr.hesco.auth.repository.PasswordResetTokenRepository;
import com.lmkr.hesco.auth.validator.PasswordHasher;
import com.lmkr.hesco.auth.validator.PasswordPolicyValidator;
import com.lmkr.hesco.user.entity.AppUser;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int RESET_TOKEN_TTL_MINUTES = 15;
    private static final int RESET_RATE_LIMIT_PER_HOUR = 3;
    private static final int LOGIN_HISTORY_DEFAULT_LIMIT = 50;

    private final AuthUserRepository authUserRepository;
    private final JwtService jwtService;
    private final PasswordHasher passwordHasher;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordChangeAuditRepository passwordChangeAuditRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final LoginHistoryRecorder loginHistoryRecorder;
    private final SecureTokenService secureTokenService;

    @Value("${auth.password.expiry-days:30}")
    private long passwordExpiryDays;

    @Value("${auth.password.expiry-warning-days:7}")
    private long passwordExpiryWarningDays;

    /**
     * A real BCrypt hash is always exactly 60 chars and starts with $2a$/
     * $2b$/$2y$. Legacy HESCO data (data-users.csv) stores plaintext
     * passwords like "12345" in the same column this code now treats as
     * passwordHash - those values fail this check and fall through to the
     * legacy path below.
     */
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    // =====================================================================
    // LOGIN
    // =====================================================================
    @Transactional
    public LoginResponse login(String username, String rawPassword, String ipAddress, String userAgent) {
        OffsetDateTime attemptAt = OffsetDateTime.now();

        AppUser user = authUserRepository.findByUsername(username).orElse(null);
        if (user == null) {
            loginHistoryRecorder.record(null, username, attemptAt, ipAddress, userAgent,
                    LoginHistory.Status.FAILURE, "Unknown username");
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (!user.isActive()) {
            loginHistoryRecorder.record(user, username, attemptAt, ipAddress, userAgent,
                    LoginHistory.Status.FAILURE, "Inactive account");
            throw new InactiveAccountException("This account is inactive");
        }

        // Assumption (unchanged from the original patch, still unverified):
        // roles with requiresImei == true are mobile-surveyor roles and
        // authenticate via the mobile app's IMEI-bound flow, not this web
        // login endpoint.
        if (user.getRole() != null && user.getRole().isRequiresImei()) {
            loginHistoryRecorder.record(user, username, attemptAt, ipAddress, userAgent,
                    LoginHistory.Status.FAILURE, "Mobile-only role");
            throw new MobileLoginNotAllowedException(
                    "This role authenticates via the mobile app, not the web login");
        }

        String storedHash = user.getPasswordHash();
        boolean authenticated = false;
        boolean justMigratedFromLegacy = false;

        if (storedHash != null) {
            if (isBcryptHash(storedHash)) {
                authenticated = passwordHasher.matches(username, rawPassword, storedHash);
            } else {
                // Legacy plaintext row. Compare directly (constant-time),
                // and if it matches, rehash to BCrypt and persist - this
                // migrates every legacy account the first time its owner
                // successfully logs in, with no separate bulk-migration
                // script.
                authenticated = constantTimeEquals(rawPassword, storedHash);
                if (authenticated) {
                    justMigratedFromLegacy = true;
                }
            }
        }

        if (!authenticated) {
            loginHistoryRecorder.record(user, username, attemptAt, ipAddress, userAgent,
                    LoginHistory.Status.FAILURE, "Invalid password");
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (justMigratedFromLegacy) {
            user.setPasswordHash(passwordHasher.hash(username, rawPassword));
            user.setPasswordChangedAt(OffsetDateTime.now());
            authUserRepository.save(user);
        }

        boolean expired = isPasswordExpired(user);
        Long expiringInDays = getPasswordExpiringInDays(user);
        if (expired && !user.isMustChangePassword()) {
            user.setMustChangePassword(true);
            authUserRepository.save(user);
        }

        loginHistoryRecorder.record(user, username, attemptAt, ipAddress, userAgent,
                LoginHistory.Status.SUCCESS, null);

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
                roleCode, boundType, circleId, divisionId, subDivisionId,
                expired, user.isMustChangePassword(), expiringInDays);
    }

    // =====================================================================
    // CHANGE PASSWORD (self-service, requires old password)
    // =====================================================================
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword, String ipAddress) {
        AppUser user = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        String storedHash = user.getPasswordHash();
        boolean oldMatches = storedHash != null && (isBcryptHash(storedHash)
                ? passwordHasher.matches(username, oldPassword, storedHash)
                : constantTimeEquals(oldPassword, storedHash));
        if (!oldMatches) {
            throw new InvalidCredentialsException("Old password is incorrect");
        }

        applyNewPassword(user, storedHash, newPassword, ipAddress, PasswordChangeAudit.ChangeType.SELF_SERVICE);
    }

    // =====================================================================
    // FORGOT PASSWORD
    // =====================================================================
    @Transactional
    public ForgotPasswordResponse forgotPassword(String username, String ipAddress) {
        AppUser user = authUserRepository.findByUsername(username).orElse(null);

        // Same response shape whether or not the account exists, so a
        // caller can't enumerate valid usernames by watching for a
        // different response.
        if (user == null) {
            return new ForgotPasswordResponse(
                    "If that account exists, a reset token has been generated.", null, null);
        }

        OffsetDateTime windowStart = OffsetDateTime.now().minusHours(1);
        long recentCount = passwordResetTokenRepository.countByUserIdAndCreatedAtAfter(user.getId(), windowStart);
        if (recentCount >= RESET_RATE_LIMIT_PER_HOUR) {
            throw new RateLimitExceededException(
                    "Too many password reset requests for this account - try again later");
        }

        String rawToken = secureTokenService.generateRawToken();
        String tokenHash = secureTokenService.hash(rawToken);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTES);

        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .requestedIp(ipAddress)
                .build());

        return new ForgotPasswordResponse(
                "If that account exists, a reset token has been generated.",
                rawToken, expiresAt.toInstant());
    }

    // =====================================================================
    // RESET PASSWORD (via token, no old password needed)
    // =====================================================================
    @Transactional
    public void resetPassword(String rawToken, String newPassword, String ipAddress) {
        String tokenHash = secureTokenService.hash(rawToken);
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .filter(PasswordResetToken::isUsable)
                .orElseThrow(() -> new InvalidResetTokenException("This reset link is invalid or has expired"));

        AppUser user = resetToken.getUser();
        String storedHash = user.getPasswordHash();

        applyNewPassword(user, storedHash, newPassword, ipAddress, PasswordChangeAudit.ChangeType.FORCED_RESET);

        resetToken.setUsedAt(OffsetDateTime.now());
        passwordResetTokenRepository.save(resetToken);
    }

    // =====================================================================
    // LOGIN HISTORY
    // =====================================================================
    @Transactional(readOnly = true)
    public List<LoginHistoryEntryResponse> getLoginHistory(String username) {
        AppUser user = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        return loginHistoryRepository.findByUserIdOrderByLoginAtDesc(user.getId(),
                        PageRequest.of(0, LOGIN_HISTORY_DEFAULT_LIMIT))
                .stream()
                .map(LoginHistoryEntryResponse::from)
                .toList();
    }

    // =====================================================================
    // Shared: policy check + reuse check + history archive + persist,
    // used by both changePassword() and resetPassword() so the two flows
    // can't drift apart on what "setting a new password" actually does.
    // =====================================================================
    private void applyNewPassword(AppUser user, String currentStoredHash, String newPassword,
                                   String ipAddress, PasswordChangeAudit.ChangeType changeType) {
        if (currentStoredHash != null && isBcryptHash(currentStoredHash)
                && passwordHasher.matches(user.getUsername(), newPassword, currentStoredHash)) {
            throw new PasswordReuseException("New password must be different from your current password");
        }

        passwordPolicyValidator.validateComplexity(user.getUsername(), newPassword);
        passwordPolicyValidator.validateNotReused(user.getId(), user.getUsername(), newPassword);

        // Archive the password being superseded - not the new one - so
        // "last 5" always reflects passwords that were actually active at
        // some point, never the one currently in use.
        if (currentStoredHash != null) {
            passwordHistoryRepository.save(PasswordHistory.builder()
                    .user(user)
                    .passwordHash(currentStoredHash)
                    .build());
        }

        user.setPasswordHash(passwordHasher.hash(user.getUsername(), newPassword));
        user.setPasswordChangedAt(OffsetDateTime.now());
        user.setMustChangePassword(false);
        authUserRepository.save(user);

        passwordChangeAuditRepository.save(PasswordChangeAudit.builder()
                .user(user)
                .ipAddress(ipAddress)
                .changeType(changeType)
                .build());
    }

    private boolean isBcryptHash(String value) {
        return BCRYPT_PATTERN.matcher(value).matches();
    }

    /**
     * Plain String.equals() on a password comparison is a timing-attack
     * surface. Legacy plaintext passwords in this dataset are already
     * about as weak as it gets, but there's no reason to add an
     * additional, easily-avoidable side channel on top of that while
     * they're mid-migration to real hashes.
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

    private boolean isPasswordExpired(AppUser user) {
        if (user.getPasswordChangedAt() == null) {
            return false; // legacy account, never tracked - no baseline to measure against
        }
        return OffsetDateTime.now().isAfter(user.getPasswordChangedAt().plusDays(passwordExpiryDays));
    }

    /**
     * Null when there's no baseline to count down from (legacy,
     * never-tracked account). Otherwise the actual days remaining - 0 if
     * already expired, and still returned (not null) even outside the
     * warning window, since the caller (login response) always wants an
     * accurate number once there IS a baseline; UI-level "only show a
     * warning inside 7 days" is a display decision, not something this
     * method should encode by returning null.
     */
    private Long getPasswordExpiringInDays(AppUser user) {
        if (user.getPasswordChangedAt() == null) {
            return null;
        }
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiry = user.getPasswordChangedAt().plusDays(passwordExpiryDays);
        if (now.isAfter(expiry)) {
            return 0L;
        }
        return Duration.between(now, expiry).toDays();
    }

    public static void main(String [] args){
        PasswordHasher passwordHasher1 = new PasswordHasher();
        String hashedPassword = passwordHasher1.hash("hesco.admin", "Lmkt@123456");
        System.out.println(hashedPassword);
    }
}
