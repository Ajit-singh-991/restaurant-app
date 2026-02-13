package com.restaurant.controller;

import com.restaurant.dto.OrderDto;
import com.restaurant.entity.Order;
import com.restaurant.entity.User;
import com.restaurant.repository.UserRepository;
import com.restaurant.security.UserPrincipal;
import com.restaurant.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderDto.CreateRequest request,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        boolean isCustomer = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> "ROLE_CUSTOMER".equals(a));
        User waiter = isCustomer ? null : user;
        User customer = isCustomer ? user : null;
        return ResponseEntity.ok(orderService.createOrder(request, waiter, customer));
    }

    @GetMapping
    public ResponseEntity<Page<Order>> getAllOrders(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Order>> getActiveOrders() {
        return ResponseEntity.ok(orderService.getActiveOrders());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<List<Order>> getActiveTableOrders(@PathVariable Long tableId) {
        return ResponseEntity.ok(orderService.getActiveTableOrders(tableId));
    }

    @GetMapping("/stats")
    public ResponseEntity<OrderDto.OrderStats> getOrderStats() {
        return ResponseEntity.ok(orderService.getOrderStats());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id,
                                               @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PutMapping("/{id}/items")
    public ResponseEntity<Order> updateOrderItems(@PathVariable Long id,
                                                   @Valid @RequestBody List<OrderDto.OrderItemRequest> items) {
        return ResponseEntity.ok(orderService.updateOrderItems(id, items));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id,
                                              @RequestBody(required = false) OrderDto.CancelRequest request) {
        String reason = request != null ? request.getReason() : null;
        return ResponseEntity.ok(orderService.cancelOrder(id, reason));
    }

    @PostMapping("/{id}/split")
    public ResponseEntity<OrderDto.SplitResponse> calculateSplit(@PathVariable Long id,
                                                                 @RequestBody @Valid OrderDto.SplitRequest request) {
        return ResponseEntity.ok(orderService.calculateSplit(id, request));
    }
}
