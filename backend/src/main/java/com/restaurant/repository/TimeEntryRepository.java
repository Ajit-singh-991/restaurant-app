package com.restaurant.repository;

import com.restaurant.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {

    Optional<TimeEntry> findByStaffIdAndClockOutIsNull(Long staffId);

    List<TimeEntry> findByStaffIdOrderByClockInDesc(Long staffId);

    List<TimeEntry> findByStaffIdAndClockInBetweenOrderByClockInDesc(Long staffId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM TimeEntry t WHERE t.clockIn >= :start AND t.clockIn < :end ORDER BY t.clockIn DESC")
    List<TimeEntry> findAllInRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t FROM TimeEntry t WHERE t.clockOut IS NULL")
    List<TimeEntry> findActiveSessions();

    @Query("SELECT t.staff.id, COALESCE(SUM(t.tips), 0) FROM TimeEntry t WHERE t.clockIn BETWEEN :start AND :end GROUP BY t.staff.id")
    List<Object[]> sumTipsByStaffBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
