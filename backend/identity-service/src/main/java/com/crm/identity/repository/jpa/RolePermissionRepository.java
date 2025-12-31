package com.crm.auth.repository.jpa;

import com.crm.auth.entity.RolePermission;
import com.crm.auth.entity.id.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

}
