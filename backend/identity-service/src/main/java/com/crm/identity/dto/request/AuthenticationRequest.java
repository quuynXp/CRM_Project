package com.crm.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
    @NotBlank
    private String identifier; // Email or Phone or CCCD
    @NotBlank
    private String password;
}