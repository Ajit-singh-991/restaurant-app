package com.restaurant.controller;

import com.restaurant.dto.AnalyticsDto;
import com.restaurant.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/analytics")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDto.DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }

    @GetMapping("/sales")
    public ResponseEntity<AnalyticsDto.SalesReport> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(analyticsService.getSalesReport(start, end));
    }

    @GetMapping("/menu")
    public ResponseEntity<AnalyticsDto.MenuAnalytics> getMenuAnalytics() {
        return ResponseEntity.ok(analyticsService.getMenuAnalytics());
    }

    @GetMapping("/customers")
    public ResponseEntity<AnalyticsDto.CustomerAnalytics> getCustomerAnalytics() {
        return ResponseEntity.ok(analyticsService.getCustomerAnalytics());
    }

    @GetMapping("/staff")
    public ResponseEntity<AnalyticsDto.StaffPerformance> getStaffPerformance() {
        return ResponseEntity.ok(analyticsService.getStaffPerformance());
    }

    @GetMapping("/inventory")
    public ResponseEntity<AnalyticsDto.InventoryReport> getInventoryReport() {
        return ResponseEntity.ok(analyticsService.getInventoryReport());
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportReport(
            @RequestParam AnalyticsDto.ReportType type,
            @RequestParam AnalyticsDto.ExportFormat format,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        byte[] body = analyticsService.exportReport(type, format, start, end);
        String filename = type.name().toLowerCase() + "-report." + (format == AnalyticsDto.ExportFormat.CSV ? "csv" : format == AnalyticsDto.ExportFormat.PDF ? "pdf" : "xlsx");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok().headers(headers).body(body);
    }
}
