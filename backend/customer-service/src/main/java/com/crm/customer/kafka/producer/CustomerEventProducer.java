package com.crm.customer.kafka.producer;

import com.crm.customer.kafka.config.KafkaConfig;
import com.crm.customer.kafka.event.CustomerCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCustomerCreatedEvent(CustomerCreatedEvent event) {
        log.info("Publishing CustomerCreatedEvent to topic {}: {}", KafkaConfig.CUSTOMER_CREATED_TOPIC, event);
        kafkaTemplate.send(KafkaConfig.CUSTOMER_CREATED_TOPIC, event.getCustomerId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Sent event=[{}] with offset=[{}]", event, result.getRecordMetadata().offset());
                    } else {
                        log.error("Unable to send event=[{}] due to : {}", event, ex.getMessage());
                    }
                });
    }
}
