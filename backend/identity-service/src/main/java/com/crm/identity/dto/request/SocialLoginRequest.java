package com.crm.auth.dto.request;

public record SocialLoginRequest(
    String idToken, 
    String accessToken
) {}
