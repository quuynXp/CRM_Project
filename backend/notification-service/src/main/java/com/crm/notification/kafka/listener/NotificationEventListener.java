package com.crm.notification.kafka.listener;

import com.crm.notification.kafka.event.NotificationEvent;
import com.crm.notification.kafka.handler.NotificationEventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationEventHandler eventHandler;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "notification.send",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, Object> record, Acknowledgment acknowledgment) {
        try {
            Object value = record.value();
            NotificationEvent event;

            if (value instanceof String) {
                event = objectMapper.readValue((String) value, NotificationEvent.class);
            } else if (value instanceof Map) {
                event = objectMapper.convertValue(value, NotificationEvent.class);
            } else {
                throw new IllegalArgumentException("Unsupported message type: " + value.getClass());
            }

            eventHandler.handleSendNotification(event);
            
        } catch (Exception e) {
            log.error("Error processing Kafka message key: {}, offset: {}", record.key(), record.offset(), e);
        } finally {
            acknowledgment.acknowledge();
        }
    }
}
