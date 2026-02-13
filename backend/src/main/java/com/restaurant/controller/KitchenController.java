package com.restaurant.controller;

import com.restaurant.dto.KitchenDto;
import com.restaurant.entity.KitchenStation;
import com.restaurant.entity.Order;
import com.restaurant.entity.OrderItem;
import com.restaurant.service.KitchenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kitchen")
@PreAuthorize("hasAnyRole('KITCHEN', 'ADMIN')")
@RequiredArgsConstructor
public class KitchenController {

    private final KitchenService kitchenService;

    @GetMapping("/stations")
    public ResponseEntity<List<KitchenStation>> getStations() {
        return ResponseEntity.ok(kitchenService.getStations());
    }

    @GetMapping("/orders/station/{stationId}")
    public ResponseEntity<List<Order>> getOrdersByStation(@PathVariable Long stationId) {
        return ResponseEntity.ok(kitchenService.getActiveOrdersByStation(stationId));
    }

    @GetMapping("/orders/active")
    public ResponseEntity<List<Order>> getActiveOrders() {
        return ResponseEntity.ok(kitchenService.getActiveOrders());
    }

    @PostMapping("/orders/{orderId}/start")
    public ResponseEntity<Order> startPreparing(@PathVariable Long orderId) {
        return ResponseEntity.ok(kitchenService.startPreparing(orderId));
    }

    @PostMapping("/orders/{orderId}/ready")
    public ResponseEntity<Order> markReady(@PathVariable Long orderId) {
        return ResponseEntity.ok(kitchenService.markReady(orderId));
    }

    @PostMapping("/orders/{orderId}/items/{itemId}/complete")
    public ResponseEntity<OrderItem> markItemComplete(@PathVariable Long orderId, @PathVariable Long itemId) {
        return ResponseEntity.ok(kitchenService.markItemComplete(orderId, itemId));
    }

    @GetMapping("/stats")
    public ResponseEntity<KitchenDto.KitchenStats> getKitchenStats() {
        return ResponseEntity.ok(kitchenService.getKitchenStats());
    }
}
