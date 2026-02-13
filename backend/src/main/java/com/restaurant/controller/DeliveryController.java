package com.restaurant.controller;

import com.restaurant.dto.DeliveryDto.*;
import com.restaurant.entity.DeliveryTracking;
import com.restaurant.security.UserPrincipal;
import com.restaurant.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(@RequestBody CreateDeliveryRequest request) {
        return ResponseEntity.ok(deliveryService.createDelivery(request));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.getByOrderId(orderId));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DeliveryResponse> assignDriver(
            @PathVariable Long id, @RequestBody AssignDriverRequest request) {
        return ResponseEntity.ok(deliveryService.assignDriver(id, request.getDriverId(), request.getEstimatedMinutes()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable Long id, @RequestParam DeliveryTracking.DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, status));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<DeliveryResponse>> getActiveDeliveries() {
        return ResponseEntity.ok(deliveryService.getActiveDeliveries());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<DeliveryResponse>> getPendingDeliveries() {
        return ResponseEntity.ok(deliveryService.getPendingDeliveries());
    }

    @GetMapping("/my")
    public ResponseEntity<List<DeliveryResponse>> getMyDeliveries(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(deliveryService.getDriverDeliveries(principal.getId()));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DeliveryStats> getStats() {
        return ResponseEntity.ok(deliveryService.getDeliveryStats());
    }
}
