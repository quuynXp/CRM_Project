package com.crm.notification.service;

import com.crm.notification.dto.request.MemorizationRequest;
import com.crm.notification.dto.response.MemorizationResponse;
import com.crm.notification.entity.UserMemorization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

public interface UserMemorizationService {
    MemorizationResponse saveMemorization(MemorizationRequest request, UUID authenticatedUserId);
    MemorizationResponse updateMemorization(UUID memorizationId, MemorizationRequest request, UUID authenticatedUserId);
    void deleteMemorization(UUID memorizationId, UUID authenticatedUserId);
    Page<MemorizationResponse> getMemorizationsByUser(UUID userId, String contentType, Pageable pageable);

    Page<MemorizationResponse> searchMemorizations(String keyword, int page, int size, Map<String, Object> filters);
}