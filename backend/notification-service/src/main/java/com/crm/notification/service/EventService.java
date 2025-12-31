package com.crm.notification.service;

import com.crm.notification.dto.request.EventRequest;
import com.crm.notification.dto.response.EventResponse;
import com.crm.notification.dto.response.MemorySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface EventService {
    Page<EventResponse> getAllEvents(String eventType, Pageable pageable);
    EventResponse getEventById(UUID id);
    EventResponse createEvent(EventRequest request);
    EventResponse updateEvent(UUID id, EventRequest request);
    void deleteEvent(UUID id);
    List<MemorySummaryResponse> findMutualMemories(UUID userA, UUID userB);
}