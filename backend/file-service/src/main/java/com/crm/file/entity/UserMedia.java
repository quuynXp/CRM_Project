package com.crm.file.entity;

import com.crm.file.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_media")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    private String fileName;

    private String filePath;

    private String fileUrl;

    private OffsetDateTime createdAt;
}
