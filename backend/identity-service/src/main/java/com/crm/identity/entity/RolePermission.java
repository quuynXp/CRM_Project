package com.crm.auth.entity;

import com.crm.auth.entity.base.BaseEntity;
import com.crm.auth.entity.id.RolePermissionId;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "role_permissions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RolePermission extends BaseEntity {

    @EmbeddedId
    private RolePermissionId id;

}
