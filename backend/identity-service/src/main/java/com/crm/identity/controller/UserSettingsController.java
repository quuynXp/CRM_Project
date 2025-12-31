package com.crm.auth.controller;

import com.crm.notification.dto.response.AppApiResponse;
import com.crm.notification.entity.User;
import com.crm.notification.entity.UserSettings;
import com.crm.notification.exception.AppException;
import com.crm.notification.exception.ErrorCode;
import com.crm.notification.repository.jpa.UserRepository;
import com.crm.notification.repository.jpa.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user-settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;

    @GetMapping("/{userId}")
    public AppApiResponse<UserSettings> getSettings(@PathVariable UUID userId) {
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        return AppApiResponse.<UserSettings>builder()
                .code(200)
                .result(settings)
                .build();
    }

    @PatchMapping("/{userId}")
    public AppApiResponse<UserSettings> updateSettings(
            @PathVariable UUID userId,
            @RequestBody UserSettings request
    ) {
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        settings.setStudyReminders(request.isStudyReminders());
        settings.setStreakReminders(request.isStreakReminders());
        settings.setSoundEnabled(request.isSoundEnabled());
        settings.setVibrationEnabled(request.isVibrationEnabled());
        settings.setProfileVisibility(request.isProfileVisibility());
        settings.setProgressSharing(request.isProgressSharing());
        settings.setSearchPrivacy(request.isSearchPrivacy());
        settings.setAutoTranslate(request.isAutoTranslate());
        settings.setSoundEnabled(request.isSoundEnabled());
        settings.setStudyReminders(request.isStudyReminders());

        return AppApiResponse.<UserSettings>builder()
                .code(200)
                .result(userSettingsRepository.save(settings))
                .build();
    }

    private UserSettings createDefaultSettings(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        UserSettings defaultSettings = UserSettings.builder()
                .user(user)
                .studyReminders(true)
                .streakReminders(true)
                .soundEnabled(true)
                .vibrationEnabled(true)
                .profileVisibility(true)
                .progressSharing(false)
                .autoTranslate(false)
                .searchPrivacy(true)
                .build();
        
        return userSettingsRepository.save(defaultSettings);
    }
}