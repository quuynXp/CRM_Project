package com.crm.auth.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;


public record OtpRequest(
    @Valid @NotBlank String emailOrPhone)
     {
        
     }
