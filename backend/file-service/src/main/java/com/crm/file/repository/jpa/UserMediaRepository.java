package com.crm.file.repository.jpa;

import com.crm.file.entity.UserMedia;
import com.crm.file.enums.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserMediaRepository extends JpaRepository<UserMedia, UUID> {
    List<UserMedia> findByUserIdAndMediaType(UUID userId, MediaType mediaType);
    List<UserMedia> findByUserId(UUID userId);
    boolean existsByFilePath(String filePath);
}
