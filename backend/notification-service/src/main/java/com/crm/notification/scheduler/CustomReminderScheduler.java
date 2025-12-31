package com.crm.notification.scheduler;

import com.crm.notification.dto.request.NotificationRequest;
import com.crm.notification.entity.User;
import com.crm.notification.entity.UserMemorization;
import com.crm.notification.enums.RepeatType;
import com.crm.notification.repository.jpa.UserMemorizationRepository;
import com.crm.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomReminderScheduler {

    private final UserMemorizationRepository userMemorizationRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void processNoteReminders() {
        List<UserMemorization> activeReminders = userMemorizationRepository.findByReminderEnabledTrue();

        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);

        for (UserMemorization note : activeReminders) {
            try {
                if (note.getReminderTime() == null) continue;

                ZoneId userZone = ZoneId.of(7);
                ZonedDateTime userNow = nowUtc.atZoneSameInstant(userZone);

                LocalTime reminderTime = LocalTime.parse(note.getReminderTime(), DateTimeFormatter.ofPattern("HH:mm"));

                if (userNow.getHour() == reminderTime.getHour() && userNow.getMinute() == reminderTime.getMinute()) {
                    
                    sendNotification(note);

                    if (note.getRepeatType() == RepeatType.ONCE) {
                        note.setReminderEnabled(false);
                        userMemorizationRepository.save(note);
                    }
                }
            } catch (Exception e) {
                log.error("Error processing reminder for note {}: {}", note.getMemorizationId(), e.getMessage());
            }
        }
    }

    private void sendNotification(UserMemorization note) {
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(note.getUserId())
                .title(note.getReminderTitle() != null ? note.getReminderTitle() : "Time to review notes!")
                .content("Review: " + note.getNoteText())
                .type("NOTE_REMINDER")
                .payload(String.format("{\"screen\":\"Notes\", \"noteId\":\"%s\"}", note.getMemorizationId()))
                .build();

        notificationService.createPushNotification(notificationRequest);
    }
}