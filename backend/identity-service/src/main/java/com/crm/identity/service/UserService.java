package com.crm.auth.service;

import com.crm.auth.dto.request.AuthenticationRequest;
import com.crm.auth.dto.request.UserRequest;
import com.crm.auth.dto.response.AuthenticationResponse;
import com.crm.auth.dto.response.UserResponse;
import com.crm.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(UUID userId, UserRequest request);
    UserResponse getUserById(UUID userId);
    void deleteUser(UUID userId);
    Page<UserResponse> getAllUsers(String keyword, Pageable pageable);
    User findUserEntity(UUID userId);
}