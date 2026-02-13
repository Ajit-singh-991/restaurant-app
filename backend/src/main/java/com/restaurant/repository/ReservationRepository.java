package com.restaurant.repository;

import com.restaurant.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByReservationDate(LocalDate date);
    List<Reservation> findByCustomerId(Long customerId);
    List<Reservation> findByReservationDateAndStatus(LocalDate date, Reservation.ReservationStatus status);
}
