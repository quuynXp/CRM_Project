package com.crm.customer.service;

import com.crm.customer.entity.Customer;
import com.crm.customer.kafka.event.CustomerCreatedEvent;
import com.crm.customer.kafka.producer.CustomerEventProducer;
import com.crm.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerCommandService {

    private final CustomerRepository customerRepository;
    private final CustomerEventProducer eventProducer;

    @Transactional
    public Customer createCustomer(Customer customer) {
        log.info("Creating new customer: {}", customer.getEmail());
        
        // 1. Save to write model (PostgreSQL)
        // customer.setId(UUID.randomUUID().toString()); // Assuming ID is generated in entity or here
        Customer savedCustomer = customerRepository.save(customer);

        // 2. Publish Domain Event to Kafka for CQRS / Event Sourcing
        CustomerCreatedEvent event = CustomerCreatedEvent.builder()
                .customerId(savedCustomer.getId())
                .fullName(savedCustomer.getFullName())
                .email(savedCustomer.getEmail())
                .phoneNumber(savedCustomer.getPhoneNumber())
                .address(savedCustomer.getAddress())
                .timestamp(System.currentTimeMillis())
                .build();
                
        eventProducer.publishCustomerCreatedEvent(event);
        
        log.info("Customer {} created and event published.", savedCustomer.getId());
        return savedCustomer;
    }
}
