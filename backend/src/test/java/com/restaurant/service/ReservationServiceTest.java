package com.restaurant.service;

import com.restaurant.entity.Reservation;
import com.restaurant.entity.RestaurantTable;
import com.restaurant.entity.User;
import com.restaurant.repository.ReservationRepository;
import com.restaurant.repository.TableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TableRepository tableRepository;

    @InjectMocks
    private ReservationService reservationService;

    private RestaurantTable testTable;
    private User testCustomer;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        testTable = RestaurantTable.builder()
                .id(1L)
                .tableNumber(5)
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .section("Main Hall")
                .build();

        testCustomer = User.builder()
                .id(1L)
                .username("customer1")
                .fullName("Test Customer")
                .role(User.Role.CUSTOMER)
                .build();

        testReservation = Reservation.builder()
                .id(1L)
                .customer(testCustomer)
                .table(testTable)
                .customerName("Test Customer")
                .customerPhone("555-0100")
                .customerEmail("customer@test.com")
                .reservationDate(LocalDate.of(2026, 2, 15))
                .reservationTime(LocalTime.of(19, 0))
                .partySize(4)
                .status(Reservation.ReservationStatus.PENDING)
                .specialRequests("Window seat preferred")
                .build();
    }

    @Test
    void getReservationsByDate_returnsReservations() {
        LocalDate date = LocalDate.of(2026, 2, 15);
        when(reservationRepository.findByReservationDate(date))
                .thenReturn(List.of(testReservation));

        List<Reservation> result = reservationService.getReservationsByDate(date);

        assertEquals(1, result.size());
        assertEquals(testReservation, result.get(0));
        assertEquals(date, result.get(0).getReservationDate());
        verify(reservationRepository).findByReservationDate(date);
    }

    @Test
    void getReservationsByDate_returnsEmptyList() {
        LocalDate date = LocalDate.of(2026, 3, 1);
        when(reservationRepository.findByReservationDate(date)).thenReturn(List.of());

        List<Reservation> result = reservationService.getReservationsByDate(date);

        assertTrue(result.isEmpty());
    }

    @Test
    void getReservationById_withValidId_returnsReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        Reservation result = reservationService.getReservationById(1L);

        assertEquals(testReservation, result);
        assertEquals("Test Customer", result.getCustomerName());
        assertEquals(4, result.getPartySize());
        assertEquals(LocalTime.of(19, 0), result.getReservationTime());
    }

    @Test
    void getReservationById_withInvalidId_throwsException() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservationService.getReservationById(999L));

        assertTrue(ex.getMessage().contains("Reservation not found"));
    }

    @Test
    void createReservation_withTable_createsSuccessfully() {
        Reservation newReservation = Reservation.builder()
                .customer(testCustomer)
                .table(RestaurantTable.builder().id(1L).build())
                .customerName("New Customer")
                .customerPhone("555-0200")
                .reservationDate(LocalDate.of(2026, 2, 20))
                .reservationTime(LocalTime.of(20, 0))
                .partySize(2)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        when(tableRepository.findById(1L)).thenReturn(Optional.of(testTable));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(2L);
            return r;
        });

        Reservation result = reservationService.createReservation(newReservation);

        assertNotNull(result);
        assertEquals(testTable, result.getTable());
        assertEquals("New Customer", result.getCustomerName());
        verify(tableRepository).findById(1L);
        verify(reservationRepository).save(newReservation);
    }

    @Test
    void createReservation_withoutTable_createsSuccessfully() {
        Reservation newReservation = Reservation.builder()
                .customer(testCustomer)
                .customerName("Walk-in Customer")
                .customerPhone("555-0300")
                .reservationDate(LocalDate.of(2026, 2, 20))
                .reservationTime(LocalTime.of(18, 30))
                .partySize(3)
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(3L);
            return r;
        });

        Reservation result = reservationService.createReservation(newReservation);

        assertNotNull(result);
        assertNull(result.getTable());
        verify(tableRepository, never()).findById(any());
        verify(reservationRepository).save(newReservation);
    }

    @Test
    void createReservation_withInvalidTable_throwsException() {
        Reservation newReservation = Reservation.builder()
                .table(RestaurantTable.builder().id(999L).build())
                .customerName("Customer")
                .reservationDate(LocalDate.of(2026, 2, 20))
                .reservationTime(LocalTime.of(19, 0))
                .partySize(2)
                .build();

        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(newReservation));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void updateStatus_toSeated_setsTableOccupied() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        Reservation result = reservationService.updateStatus(1L, Reservation.ReservationStatus.SEATED);

        assertEquals(Reservation.ReservationStatus.SEATED, result.getStatus());
        assertEquals(RestaurantTable.TableStatus.OCCUPIED, testTable.getStatus());
        verify(tableRepository).save(testTable);
        verify(reservationRepository).save(testReservation);
    }

    @Test
    void updateStatus_toCompleted_setsTableAvailable() {
        testTable.setStatus(RestaurantTable.TableStatus.OCCUPIED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        Reservation result = reservationService.updateStatus(1L, Reservation.ReservationStatus.COMPLETED);

        assertEquals(Reservation.ReservationStatus.COMPLETED, result.getStatus());
        assertEquals(RestaurantTable.TableStatus.AVAILABLE, testTable.getStatus());
        verify(tableRepository).save(testTable);
    }

    @Test
    void updateStatus_toCancelled_setsTableAvailable() {
        testTable.setStatus(RestaurantTable.TableStatus.RESERVED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        Reservation result = reservationService.updateStatus(1L, Reservation.ReservationStatus.CANCELLED);

        assertEquals(Reservation.ReservationStatus.CANCELLED, result.getStatus());
        assertEquals(RestaurantTable.TableStatus.AVAILABLE, testTable.getStatus());
        verify(tableRepository).save(testTable);
    }

    @Test
    void updateStatus_toNoShow_setsTableAvailable() {
        testTable.setStatus(RestaurantTable.TableStatus.RESERVED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        Reservation result = reservationService.updateStatus(1L, Reservation.ReservationStatus.NO_SHOW);

        assertEquals(Reservation.ReservationStatus.NO_SHOW, result.getStatus());
        assertEquals(RestaurantTable.TableStatus.AVAILABLE, testTable.getStatus());
        verify(tableRepository).save(testTable);
    }

    @Test
    void updateStatus_toConfirmed_doesNotChangeTableStatus() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        Reservation result = reservationService.updateStatus(1L, Reservation.ReservationStatus.CONFIRMED);

        assertEquals(Reservation.ReservationStatus.CONFIRMED, result.getStatus());
        assertEquals(RestaurantTable.TableStatus.AVAILABLE, testTable.getStatus());
        verify(tableRepository, never()).save(any());
    }

    @Test
    void updateStatus_seatedWithoutTable_doesNotSaveTable() {
        testReservation.setTable(null);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        Reservation result = reservationService.updateStatus(1L, Reservation.ReservationStatus.SEATED);

        assertEquals(Reservation.ReservationStatus.SEATED, result.getStatus());
        verify(tableRepository, never()).save(any());
    }

    @Test
    void updateStatus_withInvalidId_throwsException() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.updateStatus(999L, Reservation.ReservationStatus.SEATED));

        verify(reservationRepository, never()).save(any());
        verify(tableRepository, never()).save(any());
    }

    @Test
    void cancelReservation_delegatesToUpdateStatus() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        reservationService.cancelReservation(1L);

        assertEquals(Reservation.ReservationStatus.CANCELLED, testReservation.getStatus());
        assertEquals(RestaurantTable.TableStatus.AVAILABLE, testTable.getStatus());
        verify(tableRepository).save(testTable);
        verify(reservationRepository).save(testReservation);
    }

    @Test
    void cancelReservation_withInvalidId_throwsException() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.cancelReservation(999L));
    }
}
