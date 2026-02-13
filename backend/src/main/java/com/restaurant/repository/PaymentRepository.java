package com.restaurant.repository;

import com.restaurant.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :start AND :end AND p.status = 'SUCCESS'")
    List<Payment> findSuccessfulPaymentsBetween(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.createdAt BETWEEN :start AND :end AND p.status = 'SUCCESS'")
    BigDecimal sumSuccessfulPaymentsBetween(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.createdAt BETWEEN :start AND :end AND p.status = 'SUCCESS'")
    long countSuccessfulPaymentsBetween(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p WHERE p.createdAt BETWEEN :start AND :end AND p.status = 'SUCCESS' GROUP BY p.paymentMethod")
    List<Object[]> countByPaymentMethodBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
