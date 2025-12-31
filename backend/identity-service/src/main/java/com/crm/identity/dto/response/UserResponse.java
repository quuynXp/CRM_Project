package com.crm.auth.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID userId;
    private String email;
    private String phone;
    private String cccd;
    private String fullname;
    private String avatarUrl;
    private LocalDate dayOfBirth;
}