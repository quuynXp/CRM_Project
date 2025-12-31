package com.crm.auth.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    @Size(max = 255)
    private String email;

    @Size(min = 6, max = 255)
    private String password;

    @Size(max = 255)
    private String fullname;

    @Size(max = 20)
    private String phone;

    @Size(max = 20)
    private String cccd;

    private LocalDate dayOfBirth;
    private String avatarUrl;
}