package com.restaurant.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@Slf4j
public class FileUploadController {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Value("${app.upload.menu-dir:uploads/menu}")
    private String menuUploadDir;

    @PostMapping(value = "/menu-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UploadResponse> uploadMenuImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().build();
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Path dir = Paths.get(menuUploadDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                    : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target);

            String url = "/images/menu/" + filename;
            return ResponseEntity.ok(new UploadResponse(url, filename));
        } catch (IOException e) {
            log.error("Failed to save menu image", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public record UploadResponse(String url, String filename) {}
}
