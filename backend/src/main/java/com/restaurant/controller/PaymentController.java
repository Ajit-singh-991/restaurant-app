package com.restaurant.controller;

import com.restaurant.dto.PaymentDto;
import com.restaurant.entity.Payment;
import com.restaurant.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<Payment> processPayment(@Valid @RequestBody PaymentDto.PaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Payment> refundPayment(@PathVariable Long id,
                                                  @RequestBody PaymentDto.RefundRequest request) {
        return ResponseEntity.ok(paymentService.refundPayment(id, request.getReason()));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PaymentDto.PaymentStats> getPaymentStats() {
        return ResponseEntity.ok(paymentService.getPaymentStats());
    }

    @GetMapping("/daily-sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PaymentDto.DailySalesResponse> getDailySales() {
        return ResponseEntity.ok(new PaymentDto.DailySalesResponse(paymentService.getDailySales()));
    }
}
