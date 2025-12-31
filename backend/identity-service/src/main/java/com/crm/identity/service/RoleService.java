package com.crm.auth.service;

import com.crm.auth.dto.request.RoleRequest;
import com.crm.auth.dto.response.RoleResponse;
import com.crm.auth.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RoleService {
    Page<RoleResponse> getAllRoles(RoleName roleName, Pageable pageable);
    RoleResponse getRoleById(UUID id);
    RoleResponse createRole(RoleRequest request);
    RoleResponse updateRole(UUID id, RoleRequest request);
    void deleteRole(UUID id);

    void assignDefaultStudentRole(UUID userId);

    void assignRoleToUser(UUID userId, RoleName roleName);

    void removeRoleFromUser(UUID userId, RoleName roleName);

    boolean userHasRole(UUID userId, RoleName roleName);
}