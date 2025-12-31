package com.crm.auth.controller;

import com.crm.auth.dto.request.RoleRequest;
import com.crm.auth.dto.response.AppApiResponse;
import com.crm.auth.dto.response.RoleResponse;
import com.crm.auth.enums.RoleName;
import com.crm.auth.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management endpoints")
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get all roles", description = "Retrieve a paginated list of roles with optional filtering by roleName")
    public AppApiResponse<Page<RoleResponse>> getAllRoles(
            @Parameter(description = "Role name filter") @RequestParam(required = false) RoleName roleName,
            @Parameter(description = "Pagination and sorting") Pageable pageable) {
        return AppApiResponse.<Page<RoleResponse>>builder()
                .code(200)
                .message("Roles retrieved successfully")
                .result(roleService.getAllRoles(roleName, pageable))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get role by ID", description = "Retrieve a role by its ID")
    public AppApiResponse<RoleResponse> getRoleById(
            @Parameter(description = "Role ID") @PathVariable UUID id) {
        return AppApiResponse.<RoleResponse>builder()
                .code(200)
                .message("Role retrieved successfully")
                .result(roleService.getRoleById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create a new role", description = "Create a new role with the provided details")
    public AppApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        return AppApiResponse.<RoleResponse>builder()
                .code(201)
                .message("Role created successfully")
                .result(roleService.createRole(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update a role", description = "Update an existing role by its ID")
    public AppApiResponse<RoleResponse> updateRole(
            @Parameter(description = "Role ID") @PathVariable UUID id,
            @Valid @RequestBody RoleRequest request) {
        return AppApiResponse.<RoleResponse>builder()
                .code(200)
                .message("Role updated successfully")
                .result(roleService.updateRole(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete a role", description = "Delete a role by its ID")
    public AppApiResponse<Void> deleteRole(@Parameter(description = "Role ID") @PathVariable UUID id) {
        roleService.deleteRole(id);
        return AppApiResponse.<Void>builder()
                .code(200)
                .message("Role deleted successfully")
                .build();
    }

    @PostMapping("/assign-default/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Assign default student role", description = "Assign the default student role to a specific user")
    public AppApiResponse<Void> assignDefaultStudentRole(@PathVariable UUID userId) {
        roleService.assignDefaultStudentRole(userId);
        return AppApiResponse.<Void>builder()
                .code(200)
                .message("Default student role assigned successfully")
                .build();
    }

    @PostMapping("/assign/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Assign role to user", description = "Assign a specific role to a user")
    public AppApiResponse<Void> assignRoleToUser(
            @PathVariable UUID userId,
            @RequestParam RoleName roleName) {
        roleService.assignRoleToUser(userId, roleName);
        return AppApiResponse.<Void>builder()
                .code(200)
                .message("Role assigned successfully")
                .build();
    }

    @DeleteMapping("/remove/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Remove role from user", description = "Remove a specific role from a user")
    public AppApiResponse<Void> removeRoleFromUser(
            @PathVariable UUID userId,
            @RequestParam RoleName roleName) {
        roleService.removeRoleFromUser(userId, roleName);
        return AppApiResponse.<Void>builder()
                .code(200)
                .message("Role removed successfully")
                .build();
    }

    @GetMapping("/has-role/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Check user role", description = "Check if a user possesses a specific role")
    public AppApiResponse<Boolean> userHasRole(
            @PathVariable UUID userId,
            @RequestParam RoleName roleName) {
        return AppApiResponse.<Boolean>builder()
                .code(200)
                .message("Check successful")
                .result(roleService.userHasRole(userId, roleName))
                .build();
    }
}