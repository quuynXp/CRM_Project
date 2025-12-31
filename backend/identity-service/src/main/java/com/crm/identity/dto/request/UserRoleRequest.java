package com.crm.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleRequest {
    private UUID roleId;
    private UUID userId;
    private boolean isDeleted;
}
