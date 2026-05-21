package com.crm.customer.kafka.listener;

import com.crm.customer.entity.document.CustomerDocument;
import com.crm.customer.kafka.config.KafkaConfig;
import com.crm.customer.kafka.event.CustomerCreatedEvent;
import com.crm.customer.repository.search.CustomerSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventListener {

    private final CustomerSearchRepository searchRepository;

    @KafkaListener(topics = KafkaConfig.CUSTOMER_CREATED_TOPIC, groupId = "customer-service-group")
    public void handleCustomerCreatedEvent(CustomerCreatedEvent event) {
        log.info("Received CustomerCreatedEvent: {}", event);
        
        // CQRS: Sync read model (Elasticsearch) based on event from write model
        CustomerDocument document = CustomerDocument.builder()
                .id(event.getCustomerId())
                .fullName(event.getFullName())
                .email(event.getEmail())
                .phoneNumber(event.getPhoneNumber())
                .address(event.getAddress())
                .build();
                
        searchRepository.save(document);
        log.info("Customer {} successfully synced to Elasticsearch.", event.getCustomerId());
    }
}
