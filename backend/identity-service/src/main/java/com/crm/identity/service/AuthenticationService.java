package com.crm.auth.service;

import com.crm.auth.dto.request.AuthenticationRequest;
import com.crm.auth.dto.response.AuthenticationResponse;
import com.crm.auth.dto.response.IntrospectResponse;
import com.crm.auth.entity.User;


import java.util.Map;
import java.util.UUID;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request, String deviceId, String ip, String userAgent);
    AuthenticationResponse rotateRefreshToken(String oldRefreshToken, String deviceId, String ip, String userAgent);
    void logout(String accessToken);
    String generateAccessToken(User user);
    Map<String, Object> checkResetMethods(String identifier);
    void resetPassword(String resetToken, String newPassword);
    String verifyResetCode(String email, String code);
    String verifyPasswordResetOtp(String identifier, String code);
    void requestPasswordResetOtp(String identifier, String method);
    void sendPasswordResetCode(String email);
    void logoutAll(UUID userId);
    IntrospectResponse introspect(String token);
    void sendVerifyEmail(String email, UUID userId);
    AuthenticationResponse loginWithGoogle(String idToken, String deviceId, String ip, String userAgent);
    AuthenticationResponse loginWithFacebook(String accessToken, String deviceId, String ip, String userAgent);
    boolean requestOtp(String emailOrPhone);
    AuthenticationResponse verifyOtpAndLogin(String emailOrPhone, String code, String deviceId, String ip,
            String userAgent);
}