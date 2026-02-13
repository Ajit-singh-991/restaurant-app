package com.restaurant.service;

import com.restaurant.entity.Reservation;
import com.restaurant.entity.RestaurantTable;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TableRepository tableRepository;

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByDate(LocalDate date) {
        return reservationRepository.findByReservationDate(date);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByCustomer(Long customerId) {
        return reservationRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
    }

    @Transactional
    public Reservation createReservation(Reservation reservation) {
        if (reservation.getTable() != null) {
            RestaurantTable table = tableRepository.findById(reservation.getTable().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Table not found"));
            reservation.setTable(table);
        }
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateStatus(Long id, Reservation.ReservationStatus status) {
        Reservation reservation = getReservationById(id);
        reservation.setStatus(status);

        if (status == Reservation.ReservationStatus.SEATED && reservation.getTable() != null) {
            RestaurantTable table = reservation.getTable();
            table.setStatus(RestaurantTable.TableStatus.OCCUPIED);
            tableRepository.save(table);
        }
        if (status == Reservation.ReservationStatus.COMPLETED || status == Reservation.ReservationStatus.CANCELLED
                || status == Reservation.ReservationStatus.NO_SHOW) {
            if (reservation.getTable() != null) {
                RestaurantTable table = reservation.getTable();
                table.setStatus(RestaurantTable.TableStatus.AVAILABLE);
                tableRepository.save(table);
            }
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(Long id) {
        updateStatus(id, Reservation.ReservationStatus.CANCELLED);
    }
}
