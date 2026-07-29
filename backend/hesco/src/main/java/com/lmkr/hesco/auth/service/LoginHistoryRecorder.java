package com.lmkr.hesco.auth.service;

import com.lmkr.hesco.auth.entity.LoginHistory;
import com.lmkr.hesco.auth.repository.LoginHistoryRepository;
import com.lmkr.hesco.user.entity.AppUser;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A separate bean, not a private method on AuthService, so that
 * Propagation.REQUIRES_NEW actually takes effect - Spring's
 * @Transactional proxy is bypassed on self-invocation (calling a
 * REQUIRES_NEW method on `this` from within the same class silently
 * runs it in the caller's existing transaction instead). That matters
 * here specifically because a FAILED login throws after this is
 * called - without REQUIRES_NEW, AuthService.login()'s transaction
 * rolls back and the failure record it was supposed to audit
 * disappears with it, which defeats the entire point of §5.
 */
@Component
@RequiredArgsConstructor
public class LoginHistoryRecorder {

    private final LoginHistoryRepository loginHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AppUser user, String usernameAttempted, OffsetDateTime loginAt, String ipAddress,
                        String userAgent, LoginHistory.Status status, String failureReason) {
        loginHistoryRepository.save(LoginHistory.builder()
                .user(user)
                .usernameAttempted(usernameAttempted)
                .loginAt(loginAt)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(status)
                .failureReason(failureReason)
                .build());
    }
}
