package com.crm.auth.service;

import com.crm.auth.dto.request.PermissionRequest;
import com.crm.auth.dto.response.PermissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PermissionService {
    Page<PermissionResponse> getAllPermissions(String name, Pageable pageable);
    PermissionResponse getPermissionById(UUID id);
    PermissionResponse createPermission(PermissionRequest request);
    PermissionResponse updatePermission(UUID id, PermissionRequest request);
    void deletePermission(UUID id);
}