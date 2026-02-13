package com.restaurant.controller;

import com.restaurant.entity.Reservation;
import com.restaurant.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<Reservation>> getReservationsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reservationService.getReservationsByDate(date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Reservation>> getCustomerReservations(@PathVariable Long customerId) {
        return ResponseEntity.ok(reservationService.getReservationsByCustomer(customerId));
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation) {
        return ResponseEntity.ok(reservationService.createReservation(reservation));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<Reservation> updateStatus(@PathVariable Long id,
                                                     @RequestParam Reservation.ReservationStatus status) {
        return ResponseEntity.ok(reservationService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}
