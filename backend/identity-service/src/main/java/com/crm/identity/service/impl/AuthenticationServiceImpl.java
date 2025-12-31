package com.crm.auth.service.impl;

import com.crm.auth.dto.request.AuthenticationRequest;
import com.crm.auth.dto.response.AuthenticationResponse;
import com.crm.auth.dto.response.FacebookUserResponse;
import com.crm.auth.dto.response.IntrospectResponse;
import com.crm.auth.dto.response.UserResponse;
import com.crm.auth.entity.*;
import com.crm.auth.enums.AuthProvider;
import com.crm.auth.event.NotificationEvent;
import com.crm.auth.exception.AppException;
import com.crm.auth.exception.ErrorCode;
import com.crm.auth.repository.jpa.*;
import com.crm.auth.service.AuthenticationService;
import com.crm.auth.service.NotificationProducer;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Service
@FieldDefaults(level = PRIVATE)
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    @Value("classpath:private_key.pem")
    Resource privateKeyResource;
    @Value("classpath:public_key.pem")
    Resource publicKeyResource;
    @Value("${google.oauth.client-id}")
    String googleClientId;
    @Value("${jwt.key-id:default-key}")
    String jwtKeyId;

    final Map<String, String> verifyEmailCodes = new HashMap<>();
    final Map<String, String> resetPasswordCodes = new HashMap<>();
    final Map<String, String> otpLoginCodes = new HashMap<>();
    final Map<String, Instant> otpLoginExpiry = new HashMap<>();
    final Map<String, String> resetOtpCodes = new HashMap<>();
    final Map<String, Instant> resetOtpExpiry = new HashMap<>();
    final Map<String, String> secureResetTokens = new HashMap<>();

    final NotificationProducer notificationProducer;
    final PasswordEncoder passwordEncoder;
    final UserRepository userRepository;
    final InvalidatedTokenRepository invalidatedTokenRepository;
    final RefreshTokenRepository refreshTokenRepository;
    final UserRoleRepository userRoleRepository;
    final RestTemplate restTemplate;
    final UserAuthAccountRepository userAuthAccountRepository;

    private RSAPrivateKey getPrivateKey() throws Exception {
        try {
            byte[] keyBytes = privateKeyResource.getInputStream().readAllBytes();
            String key = new String(keyBytes, StandardCharsets.UTF_8);
            key = key.replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (Exception e) {
            log.error("Failed to load private key", e);
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
    }

    private RSAPublicKey getPublicKey() throws Exception {
        try {
            byte[] keyBytes = publicKeyResource.getInputStream().readAllBytes();
            String key = new String(keyBytes, StandardCharsets.UTF_8);
            key = key.replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        } catch (Exception e) {
            log.error("Failed to load public key", e);
            throw new AppException(ErrorCode.TOKEN_SIGNATURE_INVALID);
        }
    }

    @Override
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request, String deviceId, String ip, String userAgent) {
        User user = userRepository.findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        userAuthAccountRepository.findByUser_UserIdAndProvider(user.getUserId(), AuthProvider.EMAIL)
                .or(() -> Optional.of(userAuthAccountRepository.save(UserAuthAccount.builder()
                        .user(user)
                        .provider(AuthProvider.EMAIL)
                        .providerUserId(user.getEmail())
                        .verified(true)
                        .isPrimary(true)
                        .linkedAt(OffsetDateTime.now())
                        .build())));

        return createAndSaveTokens(user, deviceId, ip, userAgent);
    }

    @Override
    @Transactional
    public AuthenticationResponse loginWithGoogle(String idToken, String deviceId, String ip, String userAgent) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new AppException(ErrorCode.GOOGLE_TOKEN_INVALID);
            }
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String fullName = (String) payload.get("name");
            String googleUserId = payload.getSubject();
            if (email == null || googleUserId == null) {
                throw new AppException(ErrorCode.SOCIAL_USER_INFO_INVALID);
            }
            User user = findOrCreateUserAccount(email, fullName, null, AuthProvider.GOOGLE, googleUserId);
            return createAndSaveTokens(user, deviceId, ip, userAgent);
        } catch (Exception e) {
            log.error("Google login failed", e);
            throw new AppException(ErrorCode.GOOGLE_TOKEN_INVALID);
        }
    }

    @Override
    @Transactional
    public AuthenticationResponse loginWithFacebook(String accessToken, String deviceId, String ip, String userAgent) {
        try {
            String url = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
            FacebookUserResponse fb = restTemplate.getForObject(url, FacebookUserResponse.class);
            if (fb == null || fb.email() == null) {
                throw new AppException(ErrorCode.SOCIAL_USER_INFO_INVALID);
            }
            User user = findOrCreateUserAccount(fb.email(), fb.name(), null, AuthProvider.FACEBOOK, fb.id());
            return createAndSaveTokens(user, deviceId, ip, userAgent);
        } catch (Exception e) {
            log.error("Facebook login failed", e);
            throw new AppException(ErrorCode.FACEBOOK_TOKEN_INVALID);
        }
    }

    @Override
    @Transactional
    public boolean requestOtp(String emailOrPhone) {
        boolean isPhone = emailOrPhone.matches("^\\+?[0-9. ()-]{7,}$") && !emailOrPhone.contains("@");
        String key = isPhone ? normalizePhone(emailOrPhone) : emailOrPhone.toLowerCase().trim();

        try {
            String code = String.format("%06d", new Random().nextInt(999999));
            otpLoginCodes.put(key, code);
            otpLoginExpiry.put(key, Instant.now().plus(10, ChronoUnit.MINUTES));

            Map<String, Object> params = new HashMap<>();
            params.put("code", code);
            params.put("lang", Locale.getDefault().toString());

            if (isPhone) {
                notificationProducer.sendNotification(NotificationEvent.builder()
                        .channel("SMS")
                        .recipient(key)
                        .templateCode("OTP_LOGIN")
                        .paramMap(params)
                        .build());
            } else {
                notificationProducer.sendNotification(NotificationEvent.builder()
                        .channel("EMAIL")
                        .recipient(key)
                        .templateCode("OTP_LOGIN")
                        .subject("Your Login OTP")
                        .paramMap(params)
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to send OTP for {}", key, e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        return true;
    }

    @Override
    @Transactional
    public AuthenticationResponse verifyOtpAndLogin(String emailOrPhone, String code, String deviceId, String ip, String userAgent) {
        boolean isPhone = emailOrPhone.matches("^\\+?[0-9. ()-]{7,}$") && !emailOrPhone.contains("@");
        String key = isPhone ? normalizePhone(emailOrPhone) : emailOrPhone.toLowerCase().trim();

        Instant expiry = otpLoginExpiry.get(key);
        if (expiry == null || expiry.isBefore(Instant.now())) {
            otpLoginCodes.remove(key);
            otpLoginExpiry.remove(key);
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        String storedCode = otpLoginCodes.get(key);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        otpLoginCodes.remove(key);
        otpLoginExpiry.remove(key);

        User user = findOrCreateUserAccount(
                isPhone ? null : emailOrPhone,
                null,
                isPhone ? key : null,
                isPhone ? AuthProvider.PHONE : AuthProvider.EMAIL,
                key
        );
        return createAndSaveTokens(user, deviceId, ip, userAgent);
    }

    @Transactional
    public AuthenticationResponse handleRefreshToken(String refreshToken, String deviceId, String ip, String userAgent) {
        if (deviceId != null && ip != null && userAgent != null) {
            return refreshTokenWithDevice(refreshToken, deviceId, ip, userAgent);
        }
        return rotateRefreshToken(refreshToken, deviceId, ip, userAgent);
    }

    @Override
    @Transactional
    public AuthenticationResponse rotateRefreshToken(String oldRefreshToken, String deviceId, String ip, String userAgent) {
        try {
            SignedJWT jwt = SignedJWT.parse(oldRefreshToken);
            if (!jwt.verify(new RSASSAVerifier(getPublicKey())))
                throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);

            Date exp = jwt.getJWTClaimsSet().getExpirationTime();
            if (exp.before(new Date())) throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);

            String subject = jwt.getJWTClaimsSet().getSubject();
            UUID userId = UUID.fromString(subject);

            RefreshToken oldToken = refreshTokenRepository
                    .findByUserIdAndTokenAndIsRevokedFalse(userId, oldRefreshToken)
                    .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

            if (oldToken.isRevoked()) throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);

            oldToken.setRevoked(true);
            refreshTokenRepository.save(oldToken);

            User user = userRepository.findByUserIdAndIsDeletedFalse(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            String useDeviceId = (deviceId != null && !deviceId.isBlank()) ? deviceId : oldToken.getDeviceId();
            return createAndSaveTokens(user, useDeviceId, ip, userAgent);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
    }

    @Transactional
    public AuthenticationResponse refreshTokenWithDevice(String oldRefreshToken, String deviceId, String ip, String userAgent) {
        String cleanToken = (oldRefreshToken != null) ? oldRefreshToken.trim() : null;
        UUID userId = null;

        try {
            if (cleanToken == null || cleanToken.isEmpty()) throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);

            SignedJWT jwt = SignedJWT.parse(cleanToken);
            if (!jwt.verify(new RSASSAVerifier(getPublicKey()))) throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);

            Date exp = jwt.getJWTClaimsSet().getExpirationTime();
            if (exp.before(new Date())) throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);

            userId = UUID.fromString(jwt.getJWTClaimsSet().getSubject());

            RefreshToken oldToken = refreshTokenRepository
                    .findByUserIdAndTokenAndIsRevokedFalse(userId, cleanToken)
                    .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

            if (oldToken.getDeviceId() == null) {
                oldToken.setDeviceId(deviceId != null ? deviceId : "unknown");
            } else if (deviceId != null && !deviceId.equals(oldToken.getDeviceId())) {
                oldToken.setDeviceId(deviceId);
            }

            User user = userRepository.findByUserIdAndIsDeletedFalse(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            String newAccessToken = generateAccessToken(user);
            String newRefreshToken = cleanToken;
            OffsetDateTime now = OffsetDateTime.now();
            long daysLeft = ChronoUnit.DAYS.between(now, oldToken.getExpiresAt());

            if (daysLeft <= 7) {
                newRefreshToken = generateRefreshToken(user, 360);
                oldToken.setToken(newRefreshToken);
                oldToken.setExpiresAt(now.plusDays(360));
            } else {
                oldToken.setExpiresAt(now.plusDays(360));
            }

            if (ip != null) oldToken.setIp(ip);
            if (userAgent != null) oldToken.setUserAgent(userAgent);
            oldToken.setRevoked(false);

            refreshTokenRepository.save(oldToken);

            return buildResponse(user, newAccessToken, newRefreshToken);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        invalidatedTokenRepository.save(InvalidatedToken.builder().token(accessToken).build());
    }

    @Override
    @Transactional
    public void logoutAll(UUID userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId);
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }

    @Override
    @Transactional
    public IntrospectResponse introspect(String token) {
        try {
            if (invalidatedTokenRepository.existsByToken(token)) {
                return IntrospectResponse.builder().valid(false).build();
            }
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean verified = signedJWT.verify(new RSASSAVerifier(getPublicKey()));
            if (!verified) return IntrospectResponse.builder().valid(false).build();

            Date expiry = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiry.before(new Date())) return IntrospectResponse.builder().valid(false).build();

            return IntrospectResponse.builder().valid(true).build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.TOKEN_SIGNATURE_INVALID);
        }
    }

    @Override
    @Transactional
    public void sendVerifyEmail(String email, UUID userId) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        verifyEmailCodes.put(email, code);
        String verifyLink = "https://crm-app.com/verify?email=" + email + "&code=" + code;
        
        Map<String, Object> params = new HashMap<>();
        params.put("link", verifyLink);
        params.put("lang", Locale.getDefault().toString());

        try {
            notificationProducer.sendNotification(NotificationEvent.builder()
                    .channel("EMAIL")
                    .recipient(email)
                    .templateCode("VERIFY_ACCOUNT")
                    .subject("Verify your Account")
                    .paramMap(params)
                    .build());
        } catch (Exception e) {
            log.error("Send verify email failed", e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Transactional
    public void sendPasswordResetCode(String email) {
    }

    @Override
    @Transactional
    public void requestPasswordResetOtp(String identifier, String method) {
        User user = findByIdentifier(identifier);
        String code = String.format("%06d", new Random().nextInt(999999));
        String key = (method.equalsIgnoreCase("EMAIL")) ? user.getEmail() : user.getPhone();

        resetOtpCodes.put(key, code);
        resetOtpExpiry.put(key, Instant.now().plus(10, ChronoUnit.MINUTES));

        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("lang", Locale.getDefault().toString());

        try {
            if (method.equalsIgnoreCase("PHONE")) {
                notificationProducer.sendNotification(NotificationEvent.builder()
                        .channel("SMS")
                        .recipient(user.getPhone())
                        .templateCode("RESET_PASSWORD_OTP")
                        .paramMap(params)
                        .build());
            } else {
                notificationProducer.sendNotification(NotificationEvent.builder()
                        .channel("EMAIL")
                        .recipient(user.getEmail())
                        .templateCode("RESET_PASSWORD_OTP")
                        .subject("Reset Password OTP")
                        .paramMap(params)
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to send Reset OTP", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public String verifyPasswordResetOtp(String identifier, String code) {
        String key = identifier.contains("@") ? identifier : normalizePhone(identifier);
        String storedCode = resetOtpCodes.get(key);
        Instant expiry = resetOtpExpiry.get(key);

        if (storedCode == null || !storedCode.equals(code)) throw new AppException(ErrorCode.OTP_INVALID);
        if (expiry == null || expiry.isBefore(Instant.now())) throw new AppException(ErrorCode.OTP_EXPIRED);

        resetOtpCodes.remove(key);
        resetOtpExpiry.remove(key);

        String secureToken = UUID.randomUUID().toString();
        secureResetTokens.put(secureToken, key);
        return secureToken;
    }

    @Override
    @Transactional
    public String verifyResetCode(String email, String code) {
        return verifyPasswordResetOtp(email, code);
    }

    @Override
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        String identifier = secureResetTokens.get(resetToken);
        if (identifier == null) throw new AppException(ErrorCode.RESET_TOKEN_INVALID);

        User user = findByIdentifier(identifier);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        secureResetTokens.remove(resetToken);
    }

    @Override
    @Transactional
    public Map<String, Object> checkResetMethods(String identifier) {
        User user = findByIdentifier(identifier);
        List<UserAuthAccount> accounts = userAuthAccountRepository.findByUser_UserIdAndVerifiedTrue(user.getUserId());
        boolean hasEmail = accounts.stream().anyMatch(a -> a.getProvider() == AuthProvider.EMAIL);
        boolean hasPhone = accounts.stream().anyMatch(a -> a.getProvider() == AuthProvider.PHONE);

        Map<String, Object> response = new HashMap<>();
        response.put("hasEmail", hasEmail);
        response.put("hasPhone", hasPhone);
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        return response;
    }

    public User findOrCreateUserAccount(String email, String fullName, String phone, AuthProvider provider, String providerUserId) {
        String normalizedPhone = (phone == null || phone.isBlank()) ? null : normalizePhone(phone);
        Optional<UserAuthAccount> existingAuth = userAuthAccountRepository.findByProviderAndProviderUserId(provider, providerUserId);

        if (existingAuth.isPresent()) return existingAuth.get().getUser();

        Optional<User> existingUser = Optional.empty();
        if (email != null && !email.isBlank()) {
            existingUser = userRepository.findByEmailAndIsDeletedFalse(email.toLowerCase().trim());
        } else if (normalizedPhone != null) {
            existingUser = userRepository.findByPhoneAndIsDeletedFalse(normalizedPhone);
        }

        User user = existingUser.orElseGet(() -> {
            User u = User.builder()
                    .email(email != null ? email.toLowerCase().trim() : null)
                    .phone(normalizedPhone)
                    .fullname(fullName != null ? fullName : "User")
                    .createdAt(OffsetDateTime.now())
                    .build();
            return userRepository.save(u);
        });

        createAuthAccountLink(user, provider, providerUserId);
        return user;
    }

    public void createAuthAccountLink(User user, AuthProvider provider, String providerUserId) {
        Optional<UserAuthAccount> existingAuth = userAuthAccountRepository.findByUser_UserIdAndProviderAndProviderUserId(user.getUserId(), provider, providerUserId);
        if (existingAuth.isEmpty()) {
            UserAuthAccount authAccount = UserAuthAccount.builder()
                    .user(user)
                    .provider(provider)
                    .providerUserId(providerUserId)
                    .verified(true)
                    .isPrimary(userAuthAccountRepository.countByUser_UserId(user.getUserId()) == 0)
                    .linkedAt(OffsetDateTime.now())
                    .build();
            userAuthAccountRepository.save(authAccount);
        }
    }

    private AuthenticationResponse createAndSaveTokens(User user, String deviceId, String ip, String userAgent) {
        List<RefreshToken> existingTokens = refreshTokenRepository.findAllByUserId(user.getUserId());
        if (!existingTokens.isEmpty()) {
            existingTokens.forEach(t -> t.setRevoked(true));
            refreshTokenRepository.saveAll(existingTokens);
        }

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user, 30);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .deviceId(deviceId)
                .ip(ip)
                .userAgent(userAgent)
                .expiresAt(OffsetDateTime.now().plusDays(30))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return buildResponse(user, accessToken, refreshToken);
    }

    @Override
    public String generateAccessToken(User user) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUserId().toString())
                    .issuer("crm.auth.service")
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                    .claim("email", user.getEmail())
                    .claim("scope", buildScope(user.getUserId()))
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(jwtKeyId)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(new RSASSASigner(getPrivateKey()));
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
    }

    public String generateRefreshToken(User user, int days) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUserId().toString())
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plus(days, ChronoUnit.DAYS)))
                    .claim("type", "refresh")
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
            signedJWT.sign(new RSASSASigner(getPrivateKey()));
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
        }
    }

    private String buildScope(UUID userId) {
        List<Role> roles = userRoleRepository.findRolesByUserId(userId);
        return roles.stream()
                .map(role -> "ROLE_" + role.getRoleName().name())
                .collect(Collectors.joining(" "));
    }

    private AuthenticationResponse buildResponse(User user, String at, String rt) {
        UserResponse userResp = UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .cccd(user.getCccd())
                .fullname(user.getFullname())
                .avatarUrl(user.getAvatarUrl())
                .dayOfBirth(user.getDayOfBirth())
                .build();

        return AuthenticationResponse.builder()
                .accessToken(at)
                .authenticated(true)
                .user(userResp)
                .build();
    }

    private String normalizePhone(String rawPhone) {
        if (rawPhone == null) return null;
        String p = rawPhone.trim();
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber number = phoneUtil.parse(p, "VN");
            if (!phoneUtil.isValidNumber(number)) {
                return fallbackNormalize(p);
            }
            return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException | NoClassDefFoundError e) {
            return fallbackNormalize(p);
        }
    }

    private String fallbackNormalize(String p) {
        String digits = p.replaceAll("\\D+", "");
        if (digits.startsWith("0")) digits = "84" + digits.substring(1);
        if (!digits.startsWith("84")) digits = "84" + digits;
        return "+" + digits;
    }

    private User findByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return userRepository.findByEmailAndIsDeletedFalse(identifier)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        } else {
            String normalizedPhone = normalizePhone(identifier);
            return userRepository.findByPhoneAndIsDeletedFalse(normalizedPhone)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }
    }
}