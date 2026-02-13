package com.restaurant.service;

import com.restaurant.dto.StaffDto.*;
import com.restaurant.entity.TimeEntry;
import com.restaurant.entity.User;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.TimeEntryRepository;
import com.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public TimeEntryResponse clockIn(Long staffId) {
        timeEntryRepository.findByStaffIdAndClockOutIsNull(staffId)
                .ifPresent(e -> { throw new IllegalArgumentException("Already clocked in"); });

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff not found"));

        TimeEntry entry = TimeEntry.builder()
                .staff(staff)
                .clockIn(LocalDateTime.now())
                .build();

        return toResponse(timeEntryRepository.save(entry));
    }

    @Transactional
    public TimeEntryResponse clockOut(Long staffId) {
        TimeEntry entry = timeEntryRepository.findByStaffIdAndClockOutIsNull(staffId)
                .orElseThrow(() -> new IllegalArgumentException("No active clock-in found"));

        entry.clockOut();
        return toResponse(timeEntryRepository.save(entry));
    }

    @Transactional
    public TimeEntryResponse addTip(Long entryId, BigDecimal amount) {
        TimeEntry entry = timeEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Time entry not found"));

        entry.setTips(entry.getTips().add(amount));
        return toResponse(timeEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
    public TimeEntryResponse getActiveSession(Long staffId) {
        TimeEntry entry = timeEntryRepository.findByStaffIdAndClockOutIsNull(staffId)
                .orElse(null);
        return entry != null ? toResponse(entry) : null;
    }

    @Transactional(readOnly = true)
    public List<TimeEntryResponse> getStaffHistory(Long staffId) {
        return timeEntryRepository.findByStaffIdOrderByClockInDesc(staffId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimeEntryResponse> getActiveSessions() {
        return timeEntryRepository.findActiveSessions().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StaffSummary getDailySummary(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<TimeEntry> entries = timeEntryRepository.findAllInRange(start, end);
        List<TimeEntry> active = timeEntryRepository.findActiveSessions();

        BigDecimal totalHours = entries.stream()
                .filter(e -> e.getHoursWorked() != null)
                .map(TimeEntry::getHoursWorked)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTips = entries.stream()
                .map(TimeEntry::getTips)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<StaffPerformance> performance = entries.stream()
                .collect(Collectors.groupingBy(e -> e.getStaff().getId()))
                .entrySet().stream()
                .map(entry -> {
                    User staff = entry.getValue().get(0).getStaff();
                    BigDecimal hours = entry.getValue().stream()
                            .filter(e -> e.getHoursWorked() != null)
                            .map(TimeEntry::getHoursWorked)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal tips = entry.getValue().stream()
                            .map(TimeEntry::getTips)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return StaffPerformance.builder()
                            .staffId(staff.getId())
                            .staffName(staff.getFullName())
                            .role(staff.getRole().name())
                            .totalHoursWorked(hours.setScale(2, RoundingMode.HALF_UP))
                            .totalTips(tips)
                            .shiftsCount(entry.getValue().size())
                            .ordersHandled(0) // Could query order count per waiter
                            .build();
                })
                .collect(Collectors.toList());

        return StaffSummary.builder()
                .activeSessions(active.size())
                .totalHoursToday(totalHours.setScale(2, RoundingMode.HALF_UP))
                .totalTipsToday(totalTips)
                .staffPerformance(performance)
                .build();
    }

    private TimeEntryResponse toResponse(TimeEntry entry) {
        return TimeEntryResponse.builder()
                .id(entry.getId())
                .staffId(entry.getStaff().getId())
                .staffName(entry.getStaff().getFullName())
                .role(entry.getStaff().getRole().name())
                .clockIn(entry.getClockIn().toString())
                .clockOut(entry.getClockOut() != null ? entry.getClockOut().toString() : null)
                .hoursWorked(entry.getHoursWorked())
                .tips(entry.getTips())
                .notes(entry.getNotes())
                .build();
    }
}
