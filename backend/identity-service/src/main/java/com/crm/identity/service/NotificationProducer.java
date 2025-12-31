package com.crm.auth.service;

import com.crm.auth.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.notification:notification-events}")
    private String notificationTopic;

    public void sendNotification(NotificationEvent event) {
        try {
            kafkaTemplate.send(notificationTopic, event);
        } catch (Exception e) {
            log.error("Failed to send notification event to kafka: {}", event, e);
        }
    }
}