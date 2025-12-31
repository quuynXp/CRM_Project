package com.crm.notification.kafka.handler;

import com.crm.notification.dto.request.NotificationRequest;
import com.crm.notification.kafka.event.NotificationEvent;
import com.crm.notification.kafka.producer.NotificationStatusProducer;
import com.crm.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventHandler {

    private final NotificationService notificationService;
    private final NotificationStatusProducer statusProducer;
    private final ObjectMapper objectMapper;

    public void handleSendNotification(NotificationEvent event) {
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .title(event.getTitle())
                    .content(event.getContent())
                    .type(event.getType())
                    .payload(objectMapper.writeValueAsString(event.getPayload()))
                    .build();

            notificationService.createPushNotification(request);
            
            statusProducer.sendSuccess(event);

        } catch (Exception e) {
            log.error("Failed to process notification event: {}", event.getEventId(), e);
            statusProducer.sendFailure(event, e.getMessage());
        }
    }
}
