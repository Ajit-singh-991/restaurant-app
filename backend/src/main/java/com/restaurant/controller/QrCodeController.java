package com.restaurant.controller;

import com.restaurant.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/qr")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @GetMapping(value = "/table/{tableId}", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<byte[]> getTableQrCode(
            @PathVariable Long tableId,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height) {
        byte[] qrImage = qrCodeService.generateTableQrCode(tableId, width, height);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

    @GetMapping("/table/{tableId}/url")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> getTableOrderUrl(@PathVariable Long tableId) {
        String url = qrCodeService.getTableOrderUrl(tableId);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
