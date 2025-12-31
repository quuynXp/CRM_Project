package com.crm.notification.kafka.producer;

import com.crm.notification.kafka.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationStatusProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_NOTIFICATION_STATUS = "notification.status";

    public void sendSuccess(NotificationEvent originalEvent) {
        originalEvent.setStatus("SUCCESS");
        originalEvent.setErrorMessage(null);
        kafkaTemplate.send(TOPIC_NOTIFICATION_STATUS, originalEvent.getUserId().toString(), originalEvent);
    }

    public void sendFailure(NotificationEvent originalEvent, String errorMessage) {
        originalEvent.setStatus("FAILED");
        originalEvent.setErrorMessage(errorMessage);
        kafkaTemplate.send(TOPIC_NOTIFICATION_STATUS, originalEvent.getUserId().toString(), originalEvent);
    }
}
