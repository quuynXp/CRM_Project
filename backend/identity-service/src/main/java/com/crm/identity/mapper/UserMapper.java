package com.crm.auth.mapper;

import com.crm.auth.dto.request.UserRequest;
import com.crm.auth.dto.response.UserResponse;
import com.crm.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // ==========================================================
    // 1. REQUEST -> ENTITY (Tạo mới User)
    // ==========================================================
    User toEntity(UserRequest request);


    // ==========================================================
    // 2. ENTITY -> RESPONSE (Hiển thị User)
    // ==========================================================

    UserResponse toResponse(User entity);
    
    // ==========================================================
    // 3. UPDATE ENTITY (Cập nhật User)
    // ==========================================================
    void updateEntityFromRequest(UserRequest request, @MappingTarget User entity);
}