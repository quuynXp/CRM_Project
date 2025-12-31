package com.crm.file.controller;

import com.crm.file.entity.UserMedia;
import com.crm.file.repository.UserMediaRepository;
import com.crm.file.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileController {

    private final CloudinaryService cloudinaryService;
    private final UserMediaRepository userMediaRepository;

    @PostMapping(value = "/upload-temp", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadTemp(@RequestPart("file") MultipartFile file) {
        Map<?, ?> result = cloudinaryService.upload(file, "temp", "auto");
        
        return ResponseEntity.ok(Map.of(
            "fileUrl", result.get("secure_url"),
            "publicId", result.get("public_id"),
            "fileName", file.getOriginalFilename(),
            "format", result.get("format")
        ));
    }

    @PostMapping("/commit")
    public ResponseEntity<?> commitFile(@RequestParam String tempPublicId, 
                                      @RequestParam String targetFolder,
                                      @RequestParam UUID userId,
                                      @RequestParam String mediaType) {
        
        String newPublicId = targetFolder + "/" + tempPublicId.substring(tempPublicId.lastIndexOf('/') + 1);
        
        Map<?, ?> result = cloudinaryService.move(tempPublicId, newPublicId, true, mediaType);
        
        String finalUrl = (String) result.get("secure_url");
        if (finalUrl == null) { 
             finalUrl = "https://res.cloudinary.com/todo/image/upload/" + newPublicId; 
        }

        UserMedia media = UserMedia.builder()
                .userId(userId)
                .mediaType(mediaType)
                .fileName(newPublicId)
                .publicId(newPublicId)
                .fileUrl(finalUrl)
                .createdAt(OffsetDateTime.now())
                .build();

        userMediaRepository.save(media);

        return ResponseEntity.ok(media);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserMedia>> getUserMedia(@PathVariable UUID userId,
                                                        @RequestParam(required = false) String type) {
        List<UserMedia> mediaList;
        if (type != null) {
            mediaList = userMediaRepository.findByUserIdAndMediaType(userId, type);
        } else {
            mediaList = userMediaRepository.findByUserId(userId);
        }
        return ResponseEntity.ok(mediaList);
    }
}