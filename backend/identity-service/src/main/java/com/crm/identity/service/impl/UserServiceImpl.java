package com.crm.auth.service.impl;

import com.crm.auth.dto.request.UserRequest;
import com.crm.auth.dto.response.UserResponse;
import com.crm.auth.entity.User;
import com.crm.auth.exception.AppException; // Assumed existing
import com.crm.auth.exception.ErrorCode;   // Assumed existing
import com.crm.auth.repository.jpa.UserRepository;
import com.crm.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (request.getPhone() != null && userRepository.existsByPhoneAndIsDeletedFalse(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        if (request.getCccd() != null && userRepository.existsByCccdAndIsDeletedFalse(request.getCccd())) {
            throw new AppException(ErrorCode.CCCD_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .cccd(request.getCccd())
                .fullname(request.getFullname())
                .password(passwordEncoder.encode(request.getPassword()))
                .dayOfBirth(request.getDayOfBirth())
                .avatarUrl(request.getAvatarUrl())
                .createdAt(OffsetDateTime.now())
                .isDeleted(false)
                .build();

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UserRequest request) {
        User user = findUserEntity(userId);

        if (request.getFullname() != null) user.setFullname(request.getFullname());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getDayOfBirth() != null) user.setDayOfBirth(request.getDayOfBirth());
        
        if (request.getCccd() != null && !request.getCccd().equals(user.getCccd())) {
            if (userRepository.existsByCccdAndIsDeletedFalse(request.getCccd())) {
                throw new AppException(ErrorCode.CCCD_ALREADY_EXISTS);
            }
            user.setCccd(request.getCccd());
        }
        
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
             if (userRepository.existsByPhoneAndIsDeletedFalse(request.getPhone())) {
                throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
            }
            user.setPhone(request.getPhone());
        }

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        return mapToResponse(findUserEntity(userId));
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = findUserEntity(userId);
        user.setDeleted(true);
        user.setDeletedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable).map(this::mapToResponse);
    }

    @Override
    public User findUserEntity(UUID userId) {
        return userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .cccd(user.getCccd())
                .fullname(user.getFullname())
                .avatarUrl(user.getAvatarUrl())
                .dayOfBirth(user.getDayOfBirth())
                .build();
    }
}