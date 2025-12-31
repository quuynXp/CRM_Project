package com.crm.auth.scheduler;

import com.crm.auth.repository.jpa.InvalidatedTokenRepository;
import com.crm.auth.repository.jpa.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMaintenanceScheduler {

    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Running daily token cleanup job...");

        invalidatedTokenRepository.deleteByExpiryTimeBefore(OffsetDateTime.now());
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
        refreshTokenRepository.deleteByIsRevokedTrue();

        log.info("Token cleanup job finished.");
    }
}