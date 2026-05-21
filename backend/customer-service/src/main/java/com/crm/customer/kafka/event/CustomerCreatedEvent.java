package com.crm.customer.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreatedEvent implements Serializable {
    private String customerId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private long timestamp;
}
