package com.crm.file.scheduler;

import com.crm.file.repository.jpa.UserMediaRepository;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageCleanupService {

    private final Drive driveService;
    private final UserMediaRepository userMediaRepository;

    @Value("${google.drive.folderId}")
    private String folderId;

    private static final DateTimeFormatter RFC3339_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC);

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupTempFiles() {
        log.info("Starting Google Drive cleanup in folder: {}", folderId);

        String twoHoursAgo = RFC3339_FORMATTER.format(Instant.now().minusSeconds(7200));
        String query = String.format("'%s' in parents and createdTime < '%s' and trashed = false", folderId, twoHoursAgo);

        try {
            List<File> expiredFiles = new ArrayList<>();
            String pageToken = null;

            do {
                FileList result = driveService.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();

                if (result.getFiles() != null) {
                    expiredFiles.addAll(result.getFiles());
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);

            if (expiredFiles.isEmpty()) {
                log.info("🧹 No expired files found in Google Drive.");
                return;
            }

            log.warn("🧹 Found {} expired files. Verifying commit status in DB...", expiredFiles.size());

            int deletedCount = 0;
            for (File file : expiredFiles) {
                try {
                    String fileId = file.getId();
                    
                    if (!userMediaRepository.existsByFilePath(fileId)) {
                        driveService.files().delete(fileId).execute();
                        log.info("🧹 Deleted uncommitted file: {} (ID: {})", file.getName(), fileId);
                        deletedCount++;
                    }
                } catch (IOException e) {
                    log.error("Failed to delete file ID: {}", file.getId(), e);
                }
            }

            log.info("🎉 Cleanup complete. Deleted {} orphan files.", deletedCount);

        } catch (IOException e) {
            log.error("Google Drive cleanup task failed", e);
        }
    }
}