package com.crm.auth.mapper;

import com.crm.auth.dto.request.RoleRequest;
import com.crm.auth.dto.response.RoleResponse;
import com.crm.auth.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role entity);
    
    void updateEntityFromRequest(RoleRequest request, @MappingTarget Role entity);
}