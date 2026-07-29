package com.lmkr.hesco.auth.api;

import com.lmkr.hesco.auth.api.dto.ChangePasswordRequest;
import com.lmkr.hesco.auth.api.dto.ForgotPasswordRequest;
import com.lmkr.hesco.auth.api.dto.ForgotPasswordResponse;
import com.lmkr.hesco.auth.api.dto.LoginHistoryEntryResponse;
import com.lmkr.hesco.auth.api.dto.LoginRequest;
import com.lmkr.hesco.auth.api.dto.LoginResponse;
import com.lmkr.hesco.auth.api.dto.ResetPasswordRequest;
import com.lmkr.hesco.auth.service.AuthService;
import com.lmkr.hesco.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * auth_policy_prompt.md's 5 endpoints. change-password and
 * login-history act on the CALLER's own account - identity comes from
 * the JWT (Authentication.getName() == the token's subject, i.e.
 * username), never from a request-body user id, since this is the one
 * module where "who is asking" has an actual authenticated answer.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        LoginResponse response = authService.login(
                request.username(), request.password(), clientIp(http), userAgent(http));
        return ApiResponse.ok(response);
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                             HttpServletRequest http, Authentication authentication) {
        authService.changePassword(authentication.getName(), request.oldPassword(), request.newPassword(),
                clientIp(http));
        return ApiResponse.ok(null, "Password changed");
    }

    @PostMapping("/forgot-password")
    public ApiResponse<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                                HttpServletRequest http) {
        ForgotPasswordResponse response = authService.forgotPassword(request.username(), clientIp(http));
        return ApiResponse.ok(response);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpServletRequest http) {
        authService.resetPassword(request.token(), request.newPassword(), clientIp(http));
        return ApiResponse.ok(null, "Password reset");
    }

    @GetMapping("/login-history")
    public ApiResponse<List<LoginHistoryEntryResponse>> loginHistory(Authentication authentication) {
        return ApiResponse.ok(authService.getLoginHistory(authentication.getName()));
    }

    /**
     * X-Forwarded-For may carry a comma-separated chain (client, proxy1,
     * proxy2, ...) when multiple proxies are in front of the app - the
     * first entry is the original client. Falls back to the raw socket
     * address when the header is absent (e.g. direct connections in
     * local/dev).
     */
    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String userAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
