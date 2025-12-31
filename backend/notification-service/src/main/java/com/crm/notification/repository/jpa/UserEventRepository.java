package com.crm.notification.repository.jpa;

import com.crm.notification.entity.UserEvent;
import com.crm.notification.entity.id.UserEventId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, UserEventId> {
    List<UserEvent> findById_UserIdAndParticipatedAtBetween(UUID userId, OffsetDateTime start, OffsetDateTime end);

}
