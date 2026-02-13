package com.restaurant.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.restaurant.entity.RestaurantTable;
import com.restaurant.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final TableRepository tableRepository;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public byte[] generateTableQrCode(Long tableId, int width, int height) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        String url = String.format("%s/menu?table=%d", frontendUrl, table.getTableNumber());

        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.MARGIN, 2,
                    EncodeHintType.CHARACTER_SET, "UTF-8"
            );
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, width, height, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public String getTableOrderUrl(Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));
        return String.format("%s/menu?table=%d", frontendUrl, table.getTableNumber());
    }
}
