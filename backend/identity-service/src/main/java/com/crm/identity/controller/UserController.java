package com.crm.auth.controller;

import com.crm.auth.dto.request.UserRequest;
import com.crm.auth.dto.response.AppApiResponse;
import com.crm.auth.dto.response.UserResponse;
import com.crm.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Users", description = "User management operations")
public class UserController {
    UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve paginated list of users (Admin only)")
    public AppApiResponse<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Pagination") Pageable pageable) {
        return AppApiResponse.<Page<UserResponse>>builder()
                .code(200)
                .message("Success")
                .result(userService.getAllUsers(keyword, pageable))
                .build();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user by ID", description = "Retrieve specific user details")
    public AppApiResponse<UserResponse> getUserById(@PathVariable UUID userId) {
        return AppApiResponse.<UserResponse>builder()
                .code(200)
                .message("Success")
                .result(userService.getUserById(userId))
                .build();
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #userId.toString() == authentication.name")
    @Operation(summary = "Update user", description = "Update user profile information")
    public AppApiResponse<UserResponse> updateUser(
            @PathVariable UUID userId,
            @RequestBody UserRequest request) {
        return AppApiResponse.<UserResponse>builder()
                .code(200)
                .message("User updated successfully")
                .result(userService.updateUser(userId, request))
                .build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete user", description = "Soft delete a user account (Admin only)")
    public AppApiResponse<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return AppApiResponse.<Void>builder()
                .code(200)
                .message("User deleted successfully")
                .build();
    }
}