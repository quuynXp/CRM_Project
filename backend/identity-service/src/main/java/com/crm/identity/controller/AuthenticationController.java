package com.crm.auth.controller;

import com.crm.auth.dto.request.AuthenticationRequest;
import com.crm.auth.dto.request.OtpRequest;
import com.crm.auth.dto.request.SocialLoginRequest;
import com.crm.auth.dto.request.UserRequest;
import com.crm.auth.dto.request.VerifyOtpRequest;
import com.crm.auth.dto.response.AppApiResponse;
import com.crm.auth.dto.response.AuthenticationResponse;
import com.crm.auth.dto.response.IntrospectResponse;
import com.crm.auth.dto.response.UserResponse;
import com.crm.auth.exception.AppException;
import com.crm.auth.exception.ErrorCode;
import com.crm.auth.service.AuthenticationService;
import com.crm.auth.service.UserService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Authentication", description = "Manage user authentication, tokens, social login and OTP")
public class AuthenticationController {
    AuthenticationService authenticationService;
    UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Create a new user account")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    public AppApiResponse<UserResponse> register(@Valid @RequestBody UserRequest request) {
        UserResponse createdUser = userService.createUser(request);
        authenticationService.sendVerifyEmail(createdUser.getEmail(), createdUser.getUserId());
        return AppApiResponse.<UserResponse>builder()
                .code(200)
                .message("User registered successfully. Please verify email.")
                .result(createdUser)
                .build();
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate via Email/Password")
    public ResponseEntity<AppApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody AuthenticationRequest request,
            @RequestHeader(value = "Device-Id", required = false, defaultValue = "") String deviceId,
            @RequestHeader(value = "X-Forwarded-For", required = false, defaultValue = "") String ip,
            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
            HttpServletResponse response) {

        AuthenticationResponse authResponse = authenticationService.authenticate(request, deviceId, ip, userAgent);
        return createAuthResponseEntity(authResponse, response, "Login successful");
    }

    @PostMapping("/google-login")
    @Operation(summary = "Google Login", description = "Authenticate via Google ID Token")
    public ResponseEntity<AppApiResponse<AuthenticationResponse>> googleLogin(
            @Valid @RequestBody SocialLoginRequest request,
            @RequestHeader(value = "Device-Id", required = false, defaultValue = "") String deviceId,
            @RequestHeader(value = "X-Forwarded-For", required = false, defaultValue = "") String ip,
            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
            HttpServletResponse response) {

        AuthenticationResponse authResponse = authenticationService.loginWithGoogle(
                request.getToken(), deviceId, ip, userAgent
        );
        return createAuthResponseEntity(authResponse, response, "Google login successful");
    }

    @PostMapping("/facebook-login")
    @Operation(summary = "Facebook Login", description = "Authenticate via Facebook Access Token")
    public ResponseEntity<AppApiResponse<AuthenticationResponse>> facebookLogin(
            @Valid @RequestBody SocialLoginRequest request,
            @RequestHeader(value = "Device-Id", required = false, defaultValue = "") String deviceId,
            @RequestHeader(value = "X-Forwarded-For", required = false, defaultValue = "") String ip,
            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
            HttpServletResponse response) {

        AuthenticationResponse authResponse = authenticationService.loginWithFacebook(
                request.getToken(), deviceId, ip, userAgent
        );
        return createAuthResponseEntity(authResponse, response, "Facebook login successful");
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh Token", description = "Rotate refresh token. Supports device tracking.")
    public ResponseEntity<AppApiResponse<AuthenticationResponse>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "Device-Id", required = false, defaultValue = "") String deviceId,
            @RequestHeader(value = "X-Forwarded-For", required = false, defaultValue = "") String ip,
            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
            HttpServletResponse response) {

        String refreshToken = (body != null && body.get("refreshToken") != null)
                ? body.get("refreshToken")
                : refreshTokenCookie;

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        AuthenticationResponse authResponse = authenticationService.handleRefreshToken(refreshToken, deviceId, ip, userAgent);
        return createAuthResponseEntity(authResponse, response, "Token refreshed successfully");
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate current session")
    public AppApiResponse<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse response) {
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authenticationService.logout(token);
        }

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return AppApiResponse.<Void>builder()
                .code(200)
                .message("Logout successful")
                .build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout All", description = "Invalidate all sessions for the user")
    public AppApiResponse<Void> logoutAll(@RequestHeader("userId") UUID userId) {
        authenticationService.logoutAll(userId);
        return AppApiResponse.<Void>builder()
                .code(200)
                .message("Logged out from all devices successfully")
                .build();
    }

    @PostMapping("/introspect")
    @Operation(summary = "Introspect", description = "Check token validity")
    public AppApiResponse<IntrospectResponse> introspect(@RequestHeader("Authorization") String authorizationHeader) 
            throws ParseException, JOSEException {
        String token = authorizationHeader.replace("Bearer ", "");
        var result = authenticationService.introspect(token);
        return AppApiResponse.<IntrospectResponse>builder()
                .code(200)
                .message("Token introspected successfully")
                .result(result)
                .build();
    }

    @PostMapping("/request-otp")
    @Operation(summary = "Request OTP", description = "Send OTP to email or phone")
    public AppApiResponse<Map<String, Boolean>> requestOtp(@Valid @RequestBody OtpRequest request) {
        boolean success = authenticationService.requestOtp(request.getEmailOrPhone());
        return AppApiResponse.<Map<String, Boolean>>builder()
                .code(200)
                .message("OTP sent successfully")
                .result(Map.of("success", success))
                .build();
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify OTP and login")
    public ResponseEntity<AppApiResponse<AuthenticationResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            @RequestHeader(value = "Device-Id", required = false, defaultValue = "") String deviceId,
            @RequestHeader(value = "X-Forwarded-For", required = false, defaultValue = "") String ip,
            @RequestHeader(value = "User-Agent", required = false, defaultValue = "") String userAgent,
            HttpServletResponse response) {

        AuthenticationResponse authResponse = authenticationService.verifyOtpAndLogin(
                request.getEmailOrPhone(), request.getCode(), deviceId, ip, userAgent
        );
        return createAuthResponseEntity(authResponse, response, "OTP verification successful");
    }

    @PostMapping("/check-reset-methods")
    public AppApiResponse<Map<String, Object>> checkResetMethods(@RequestBody Map<String, String> body) {
        String identifier = body.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            throw new AppException(ErrorCode.INVALID_INPUT_DATA);
        }
        return AppApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Reset methods retrieved")
                .result(authenticationService.checkResetMethods(identifier))
                .build();
    }

    @PostMapping("/request-password-reset-otp")
    public AppApiResponse<Void> requestPasswordResetOtp(@RequestBody Map<String, String> body) {
        String identifier = body.get("identifier");
        String method = body.get("method");

        if (identifier == null || method == null) {
            throw new AppException(ErrorCode.INVALID_INPUT_DATA);
        }
        authenticationService.requestPasswordResetOtp(identifier, method);
        return AppApiResponse.<Void>builder()
                .code(200)
                .message("Password reset OTP sent successfully")
                .build();
    }

    @PostMapping("/verify-password-reset-otp")
    public AppApiResponse<Map<String, String>> verifyPasswordResetOtp(@RequestBody Map<String, String> body) {
        String identifier = body.get("identifier");
        String code = body.get("code");
        String secureToken = authenticationService.verifyPasswordResetOtp(identifier, code);
        return AppApiResponse.<Map<String, String>>builder()
                .code(200)
                .message("OTP verified")
                .result(Map.of("resetToken", secureToken))
                .build();
    }

    @PostMapping("/reset-password")
    public AppApiResponse<Void> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("password");
        authenticationService.resetPassword(token, newPassword);
        return AppApiResponse.<Void>builder()
                .code(200)
                .message("Password reset successful")
                .build();
    }

    private ResponseEntity<AppApiResponse<AuthenticationResponse>> createAuthResponseEntity(
            AuthenticationResponse authResponse,
            HttpServletResponse response,
            String message) {

        String refreshToken = authResponse.getRefreshToken();
        
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);

        AuthenticationResponse cleanResponse = AuthenticationResponse.builder()
                .accessToken(authResponse.getAccessToken())
                .authenticated(true)
                .user(authResponse.getUser())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + authResponse.getAccessToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(AppApiResponse.<AuthenticationResponse>builder()
                        .code(200)
                        .message(message)
                        .result(cleanResponse)
                        .build());
    }
}