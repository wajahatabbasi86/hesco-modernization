package com.lmkr.hesco.user.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.user.api.dto.AppUserRequest;
import com.lmkr.hesco.user.api.dto.AppUserResponse;
import com.lmkr.hesco.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User management (SRS §3.2). All writes go through UserService so the
 * role/bound-type/IMEI rule (§3.2.3, §8.1.1) is always enforced.
 */
@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final UserService userService;

    public AppUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<AppUserResponse>> list() {
        return ApiResponse.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<AppUserResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(userService.findResponseById(id));
    }

    @PostMapping
    public ApiResponse<AppUserResponse> create(@Valid @RequestBody AppUserRequest request) {
        return ApiResponse.ok(AppUserResponse.from(userService.create(request)), "User created");
    }

    @PutMapping("/{id}")
    public ApiResponse<AppUserResponse> update(@PathVariable Long id, @Valid @RequestBody AppUserRequest request) {
        return ApiResponse.ok(AppUserResponse.from(userService.update(id, request)), "User updated");
    }
}