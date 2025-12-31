package com.crm.auth.mapper;

import com.crm.auth.dto.request.PermissionRequest;
import com.crm.auth.dto.response.PermissionResponse;
import com.crm.auth.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    Permission toEntity(PermissionRequest request);

    PermissionResponse toResponse(Permission entity);
    
    void updateEntityFromRequest(PermissionRequest request, @MappingTarget Permission entity);
}