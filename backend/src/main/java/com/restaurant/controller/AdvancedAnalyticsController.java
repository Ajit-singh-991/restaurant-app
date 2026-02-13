package com.restaurant.controller;

import com.restaurant.dto.AdvancedAnalyticsDto.*;
import com.restaurant.service.AdvancedAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics/advanced")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@RequiredArgsConstructor
public class AdvancedAnalyticsController {

    private final AdvancedAnalyticsService advancedAnalyticsService;

    @GetMapping("/forecast")
    public ResponseEntity<SalesForecast> getSalesForecast(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(advancedAnalyticsService.getSalesForecast(days));
    }

    @GetMapping("/customers/segments")
    public ResponseEntity<CustomerSegmentation> getCustomerSegmentation() {
        return ResponseEntity.ok(advancedAnalyticsService.getCustomerSegmentation());
    }

    @GetMapping("/menu/optimization")
    public ResponseEntity<MenuOptimization> getMenuOptimization() {
        return ResponseEntity.ok(advancedAnalyticsService.getMenuOptimization());
    }

    @GetMapping("/peak-hours")
    public ResponseEntity<PeakHoursAnalysis> getPeakHours() {
        return ResponseEntity.ok(advancedAnalyticsService.getPeakHoursAnalysis());
    }
}
