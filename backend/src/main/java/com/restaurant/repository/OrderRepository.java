package com.restaurant.repository;

import com.restaurant.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByStatusIn(List<Order.OrderStatus> statuses);
    List<Order> findByWaiterId(Long waiterId);
    List<Order> findByTableId(Long tableId);
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Order> findByTableIdAndStatusIn(Long tableId, List<Order.OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.createdAt ASC")
    List<Order> findActiveOrders(List<Order.OrderStatus> statuses);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt BETWEEN :start AND :end AND o.status <> 'CANCELLED'")
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o.orderType, COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :start AND :end GROUP BY o.orderType")
    List<Object[]> countByOrderTypeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :start AND :end GROUP BY o.status")
    List<Object[]> countByStatusBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT oi.menuItem.id, oi.menuItem.name, SUM(oi.quantity), SUM(oi.totalPrice) " +
           "FROM OrderItem oi WHERE oi.order.createdAt BETWEEN :start AND :end AND oi.order.status <> 'CANCELLED' " +
           "GROUP BY oi.menuItem.id, oi.menuItem.name ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingItems(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c.name, COALESCE(SUM(oi.totalPrice), 0) " +
           "FROM OrderItem oi JOIN oi.menuItem mi JOIN mi.category c " +
           "WHERE oi.order.createdAt BETWEEN :start AND :end AND oi.order.status <> 'CANCELLED' " +
           "GROUP BY c.name ORDER BY SUM(oi.totalPrice) DESC")
    List<Object[]> revenueByCategory(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('DATE', o.createdAt), COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
           "FROM Order o WHERE o.createdAt BETWEEN :start AND :end AND o.status <> 'CANCELLED' " +
           "GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt)")
    List<Object[]> dailySalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o.customer.id, COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :start AND :end " +
           "AND o.customer IS NOT NULL AND o.status <> 'CANCELLED' GROUP BY o.customer.id")
    List<Object[]> countOrdersByCustomerBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o.waiter.id, COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :start AND :end " +
           "AND o.waiter IS NOT NULL AND o.status <> 'CANCELLED' GROUP BY o.waiter.id")
    List<Object[]> countOrdersByWaiterBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.station.id = :stationId AND o.status IN :statuses ORDER BY o.createdAt ASC")
    List<Order> findActiveOrdersByStationId(@Param("stationId") Long stationId, @Param("statuses") List<Order.OrderStatus> statuses);

    List<Order> findTop10ByOrderByCreatedAtDesc();
}
