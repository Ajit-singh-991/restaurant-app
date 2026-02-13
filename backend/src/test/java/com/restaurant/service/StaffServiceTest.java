package com.restaurant.service;

import com.restaurant.dto.StaffDto.*;
import com.restaurant.entity.TimeEntry;
import com.restaurant.entity.User;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.TimeEntryRepository;
import com.restaurant.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private StaffService staffService;

    private User testStaff;
    private TimeEntry testEntry;

    @BeforeEach
    void setUp() {
        testStaff = User.builder()
                .id(1L)
                .username("waiter1")
                .fullName("Test Waiter")
                .role(User.Role.WAITER)
                .build();

        testEntry = TimeEntry.builder()
                .id(10L)
                .staff(testStaff)
                .clockIn(LocalDateTime.now().minusHours(4))
                .clockOut(null)
                .hoursWorked(null)
                .tips(BigDecimal.ZERO)
                .build();
    }

    // ---- clockIn tests ----

    @Test
    void clockIn_success_createsTimeEntry() {
        TimeEntry savedEntry = TimeEntry.builder()
                .id(10L)
                .staff(testStaff)
                .clockIn(LocalDateTime.now())
                .tips(BigDecimal.ZERO)
                .build();

        when(timeEntryRepository.findByStaffIdAndClockOutIsNull(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStaff));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenReturn(savedEntry);

        TimeEntryResponse response = staffService.clockIn(1L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getStaffId());
        assertEquals("Test Waiter", response.getStaffName());
        assertEquals("WAITER", response.getRole());
        assertNotNull(response.getClockIn());
        assertNull(response.getClockOut());
        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    @Test
    void clockIn_alreadyClockedIn_throwsException() {
        when(timeEntryRepository.findByStaffIdAndClockOutIsNull(1L)).thenReturn(Optional.of(testEntry));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> staffService.clockIn(1L));

        assertEquals("Already clocked in", exception.getMessage());
        verify(timeEntryRepository, never()).save(any());
    }

    @Test
    void clockIn_staffNotFound_throwsException() {
        when(timeEntryRepository.findByStaffIdAndClockOutIsNull(999L)).thenReturn(Optional.empty());
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> staffService.clockIn(999L));
    }

    // ---- clockOut tests ----

    @Test
    void clockOut_success_setsClockOutAndHoursWorked() {
        TimeEntry activeEntry = TimeEntry.builder()
                .id(10L)
                .staff(testStaff)
                .clockIn(LocalDateTime.now().minusHours(8))
                .tips(BigDecimal.ZERO)
                .build();

        when(timeEntryRepository.findByStaffIdAndClockOutIsNull(1L)).thenReturn(Optional.of(activeEntry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeEntryResponse response = staffService.clockOut(1L);

        assertNotNull(response);
        assertNotNull(response.getClockOut());
        assertNotNull(response.getHoursWorked());
        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    @Test
    void clockOut_noActiveClockin_throwsException() {
        when(timeEntryRepository.findByStaffIdAndClockOutIsNull(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> staffService.clockOut(1L));

        assertEquals("No active clock-in found", exception.getMessage());
        verify(timeEntryRepository, never()).save(any());
    }

    // ---- addTip tests ----

    @Test
    void addTip_success_addsTipAmount() {
        testEntry.setTips(new BigDecimal("10.00"));

        when(timeEntryRepository.findById(10L)).thenReturn(Optional.of(testEntry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeEntryResponse response = staffService.addTip(10L, new BigDecimal("15.50"));

        assertNotNull(response);
        assertEquals(new BigDecimal("25.50"), response.getTips());
        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    @Test
    void addTip_firstTip_setsFromZero() {
        testEntry.setTips(BigDecimal.ZERO);

        when(timeEntryRepository.findById(10L)).thenReturn(Optional.of(testEntry));
        when(timeEntryRepository.save(any(TimeEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TimeEntryResponse response = staffService.addTip(10L, new BigDecimal("20.00"));

        assertEquals(new BigDecimal("20.00"), response.getTips());
    }

    @Test
    void addTip_entryNotFound_throwsException() {
        when(timeEntryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> staffService.addTip(999L, new BigDecimal("10.00")));
    }

    // ---- getActiveSession tests ----

    @Test
    void getActiveSession_hasActiveSession_returnsResponse() {
        when(timeEntryRepository.findByStaffIdAndClockOutIsNull(1L)).thenReturn(Optional.of(testEntry));

        TimeEntryResponse response = staffService.getActiveSession(1L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getStaffId());
        assertNull(response.getClockOut());
    }

    @Test
    void getActiveSession_noActiveSession_returnsNull() {
        when(timeEntryRepository.findByStaffIdAndClockOutIsNull(1L)).thenReturn(Optional.empty());

        TimeEntryResponse response = staffService.getActiveSession(1L);

        assertNull(response);
    }

    // ---- getDailySummary tests ----

    @Test
    void getDailySummary_withEntries_returnsCorrectSummary() {
        User waiter2 = User.builder()
                .id(2L)
                .username("waiter2")
                .fullName("Second Waiter")
                .role(User.Role.WAITER)
                .build();

        TimeEntry entry1 = TimeEntry.builder()
                .id(1L)
                .staff(testStaff)
                .clockIn(LocalDateTime.now().minusHours(8))
                .clockOut(LocalDateTime.now())
                .hoursWorked(new BigDecimal("8.00"))
                .tips(new BigDecimal("50.00"))
                .build();

        TimeEntry entry2 = TimeEntry.builder()
                .id(2L)
                .staff(waiter2)
                .clockIn(LocalDateTime.now().minusHours(6))
                .clockOut(LocalDateTime.now())
                .hoursWorked(new BigDecimal("6.00"))
                .tips(new BigDecimal("35.00"))
                .build();

        LocalDate today = LocalDate.now();

        when(timeEntryRepository.findAllInRange(any(), any())).thenReturn(List.of(entry1, entry2));
        when(timeEntryRepository.findActiveSessions()).thenReturn(List.of());

        StaffSummary summary = staffService.getDailySummary(today);

        assertNotNull(summary);
        assertEquals(0, summary.getActiveSessions());
        assertEquals(new BigDecimal("14.00").setScale(2, RoundingMode.HALF_UP), summary.getTotalHoursToday());
        assertEquals(new BigDecimal("85.00"), summary.getTotalTipsToday());
        assertEquals(2, summary.getStaffPerformance().size());
    }

    @Test
    void getDailySummary_withActiveSessions_countsCorrectly() {
        TimeEntry activeEntry = TimeEntry.builder()
                .id(3L)
                .staff(testStaff)
                .clockIn(LocalDateTime.now().minusHours(2))
                .clockOut(null)
                .hoursWorked(null)
                .tips(new BigDecimal("10.00"))
                .build();

        LocalDate today = LocalDate.now();

        when(timeEntryRepository.findAllInRange(any(), any())).thenReturn(List.of(activeEntry));
        when(timeEntryRepository.findActiveSessions()).thenReturn(List.of(activeEntry));

        StaffSummary summary = staffService.getDailySummary(today);

        assertEquals(1, summary.getActiveSessions());
        // hoursWorked is null for active entry, so totalHours should be 0
        assertEquals(new BigDecimal("0").setScale(2, RoundingMode.HALF_UP), summary.getTotalHoursToday());
        assertEquals(new BigDecimal("10.00"), summary.getTotalTipsToday());
    }

    @Test
    void getDailySummary_noEntries_returnsEmptySummary() {
        LocalDate today = LocalDate.now();

        when(timeEntryRepository.findAllInRange(any(), any())).thenReturn(List.of());
        when(timeEntryRepository.findActiveSessions()).thenReturn(List.of());

        StaffSummary summary = staffService.getDailySummary(today);

        assertNotNull(summary);
        assertEquals(0, summary.getActiveSessions());
        assertEquals(new BigDecimal("0").setScale(2, RoundingMode.HALF_UP), summary.getTotalHoursToday());
        assertEquals(BigDecimal.ZERO, summary.getTotalTipsToday());
        assertTrue(summary.getStaffPerformance().isEmpty());
    }

    @Test
    void getDailySummary_staffPerformance_calculatesPerStaffMetrics() {
        TimeEntry shift1 = TimeEntry.builder()
                .id(1L)
                .staff(testStaff)
                .clockIn(LocalDateTime.now().minusHours(8))
                .clockOut(LocalDateTime.now().minusHours(4))
                .hoursWorked(new BigDecimal("4.00"))
                .tips(new BigDecimal("20.00"))
                .build();

        TimeEntry shift2 = TimeEntry.builder()
                .id(2L)
                .staff(testStaff)
                .clockIn(LocalDateTime.now().minusHours(3))
                .clockOut(LocalDateTime.now())
                .hoursWorked(new BigDecimal("3.00"))
                .tips(new BigDecimal("15.00"))
                .build();

        LocalDate today = LocalDate.now();

        when(timeEntryRepository.findAllInRange(any(), any())).thenReturn(List.of(shift1, shift2));
        when(timeEntryRepository.findActiveSessions()).thenReturn(List.of());

        StaffSummary summary = staffService.getDailySummary(today);

        assertEquals(1, summary.getStaffPerformance().size());

        StaffPerformance perf = summary.getStaffPerformance().get(0);
        assertEquals(1L, perf.getStaffId());
        assertEquals("Test Waiter", perf.getStaffName());
        assertEquals("WAITER", perf.getRole());
        assertEquals(new BigDecimal("7.00").setScale(2, RoundingMode.HALF_UP), perf.getTotalHoursWorked());
        assertEquals(new BigDecimal("35.00"), perf.getTotalTips());
        assertEquals(2, perf.getShiftsCount());
    }
}
