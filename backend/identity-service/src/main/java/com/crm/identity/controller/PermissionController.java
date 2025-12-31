package com.crm.auth.controller;

import com.crm.auth.dto.request.PermissionRequest;
import com.crm.auth.dto.response.AppApiResponse;
import com.crm.auth.dto.response.PermissionResponse;
import com.crm.auth.service.PermissionService;
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
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Permission management endpoints")
public class PermissionController {
    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get all permissions", description = "Retrieve a paginated list of permissions with optional filtering by name")
    public AppApiResponse<Page<PermissionResponse>> getAllPermissions(
            @Parameter(description = "Permission name filter") @RequestParam(required = false) String name,
            @Parameter(description = "Pagination and sorting") Pageable pageable) {
        return AppApiResponse.<Page<PermissionResponse>>builder()
                .code(200)
                .message("Permissions retrieved successfully")
                .result(permissionService.getAllPermissions(name, pageable))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get permission by ID", description = "Retrieve a permission by its ID")
    public AppApiResponse<PermissionResponse> getPermissionById(
            @Parameter(description = "Permission ID") @PathVariable UUID id) {
        return AppApiResponse.<PermissionResponse>builder()
                .code(200)
                .message("Permission retrieved successfully")
                .result(permissionService.getPermissionById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create a new permission", description = "Create a new permission with the provided details")
    public AppApiResponse<PermissionResponse> createPermission(@Valid @RequestBody PermissionRequest request) {
        return AppApiResponse.<PermissionResponse>builder()
                .code(201)
                .message("Permission created successfully")
                .result(permissionService.createPermission(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update a permission", description = "Update an existing permission by its ID")
    public AppApiResponse<PermissionResponse> updatePermission(
            @Parameter(description = "Permission ID") @PathVariable UUID id,
            @Valid @RequestBody PermissionRequest request) {
        return AppApiResponse.<PermissionResponse>builder()
                .code(200)
                .message("Permission updated successfully")
                .result(permissionService.updatePermission(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete a permission", description = "Delete a permission by its ID")
    public AppApiResponse<Void> deletePermission(@Parameter(description = "Permission ID") @PathVariable UUID id) {
        permissionService.deletePermission(id);
        return AppApiResponse.<Void>builder()
                .code(200)
                .message("Permission deleted successfully")
                .build();
    }
}