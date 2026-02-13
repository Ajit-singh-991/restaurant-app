package com.restaurant.controller;

import com.restaurant.dto.StaffDto.*;
import com.restaurant.security.UserPrincipal;
import com.restaurant.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping("/clock-in")
    public ResponseEntity<TimeEntryResponse> clockIn(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(staffService.clockIn(principal.getId()));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<TimeEntryResponse> clockOut(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(staffService.clockOut(principal.getId()));
    }

    @GetMapping("/my/session")
    public ResponseEntity<TimeEntryResponse> getActiveSession(@AuthenticationPrincipal UserPrincipal principal) {
        TimeEntryResponse session = staffService.getActiveSession(principal.getId());
        return session != null ? ResponseEntity.ok(session) : ResponseEntity.noContent().build();
    }

    @GetMapping("/my/history")
    public ResponseEntity<List<TimeEntryResponse>> getMyHistory(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(staffService.getStaffHistory(principal.getId()));
    }

    @PostMapping("/time-entries/{id}/tip")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TimeEntryResponse> addTip(@PathVariable Long id, @RequestBody AddTipRequest request) {
        return ResponseEntity.ok(staffService.addTip(id, request.getAmount()));
    }

    @GetMapping("/active-sessions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TimeEntryResponse>> getActiveSessions() {
        return ResponseEntity.ok(staffService.getActiveSessions());
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<StaffSummary> getDailySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(staffService.getDailySummary(date));
    }
}
