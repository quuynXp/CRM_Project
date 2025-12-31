package com.crm.notification.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {
    private String eventId;
    private UUID userId;
    private String type;
    private String title;
    private String content;
    private Map<String, Object> payload;
    private long timestamp;
    private String status; 
    private String errorMessage;
}