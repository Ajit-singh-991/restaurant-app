package com.restaurant.service;

import com.restaurant.dto.AnalyticsDto;
import com.restaurant.entity.MenuItem;
import com.restaurant.entity.Order;
import com.restaurant.entity.RestaurantTable;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.PaymentRepository;
import com.restaurant.repository.TableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private TableRepository tableRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private RestaurantTable availableTable;
    private RestaurantTable occupiedTable;

    @BeforeEach
    void setUp() {
        availableTable = RestaurantTable.builder()
                .id(1L)
                .tableNumber(1)
                .capacity(4)
                .status(RestaurantTable.TableStatus.AVAILABLE)
                .build();

        occupiedTable = RestaurantTable.builder()
                .id(2L)
                .tableNumber(2)
                .capacity(6)
                .status(RestaurantTable.TableStatus.OCCUPIED)
                .build();
    }

    @Test
    void getDashboardStats_returnsCorrectStats() {
        when(orderRepository.countByCreatedAtBetween(any(), any())).thenReturn(25L);
        when(orderRepository.sumRevenueBetween(any(), any())).thenReturn(new BigDecimal("5000.00"));
        when(orderRepository.findActiveOrders(any())).thenReturn(List.of(
                Order.builder().id(1L).status(Order.OrderStatus.PREPARING).build(),
                Order.builder().id(2L).status(Order.OrderStatus.PENDING).build()
        ));
        when(tableRepository.findAll()).thenReturn(List.of(availableTable, occupiedTable));
        when(orderRepository.countByStatusBetween(any(), any())).thenReturn(List.of(
                new Object[]{"PENDING", 5L},
                new Object[]{"PREPARING", 10L},
                new Object[]{"COMPLETED", 10L}
        ));
        when(orderRepository.countByOrderTypeBetween(any(), any())).thenReturn(List.of(
                new Object[]{"DINE_IN", 15L},
                new Object[]{"TAKEAWAY", 10L}
        ));
        when(paymentRepository.countByPaymentMethodBetween(any(), any())).thenReturn(List.of(
                new Object[]{"CARD", 15L},
                new Object[]{"CASH", 10L}
        ));
        when(orderRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(orderRepository.findTopSellingItems(any(), any())).thenReturn(List.of(
                new Object[]{1L, "Butter Chicken", 20L, new BigDecimal("7000.00")},
                new Object[]{2L, "Dal Makhani", 15L, new BigDecimal("3750.00")}
        ));

        AnalyticsDto.DashboardStats stats = analyticsService.getDashboardStats();

        assertEquals(25L, stats.getTotalOrders());
        assertEquals(new BigDecimal("5000.00"), stats.getTotalRevenue());
        assertEquals(new BigDecimal("200.00"), stats.getAverageOrderValue());
        assertEquals(2L, stats.getActiveOrders());
        assertEquals(1, stats.getTablesOccupied());
        assertEquals(2, stats.getTotalTables());
        assertEquals(3, stats.getOrdersByStatus().size());
        assertEquals(5L, stats.getOrdersByStatus().get("PENDING"));
        assertEquals(2, stats.getOrdersByType().size());
        assertEquals(15L, stats.getOrdersByType().get("DINE_IN"));
        assertEquals(2, stats.getTopSellingItems().size());
        assertEquals("Butter Chicken", stats.getTopSellingItems().get(0).getItemName());
    }

    @Test
    void getDashboardStats_withZeroOrders_returnsZeroAverageOrderValue() {
        when(orderRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(orderRepository.sumRevenueBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(orderRepository.findActiveOrders(any())).thenReturn(List.of());
        when(tableRepository.findAll()).thenReturn(List.of(availableTable));
        when(orderRepository.countByStatusBetween(any(), any())).thenReturn(List.of());
        when(orderRepository.countByOrderTypeBetween(any(), any())).thenReturn(List.of());
        when(paymentRepository.countByPaymentMethodBetween(any(), any())).thenReturn(List.of());
        when(orderRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of());
        when(orderRepository.findTopSellingItems(any(), any())).thenReturn(List.of());

        AnalyticsDto.DashboardStats stats = analyticsService.getDashboardStats();

        assertEquals(0L, stats.getTotalOrders());
        assertEquals(BigDecimal.ZERO, stats.getAverageOrderValue());
        assertEquals(0L, stats.getActiveOrders());
        assertEquals(0, stats.getTablesOccupied());
        assertEquals(1, stats.getTotalTables());
        assertTrue(stats.getOrdersByStatus().isEmpty());
        assertTrue(stats.getOrdersByType().isEmpty());
        assertTrue(stats.getTopSellingItems().isEmpty());
    }

    @Test
    void getDashboardStats_topItemsLimitedToTen() {
        when(orderRepository.countByCreatedAtBetween(any(), any())).thenReturn(100L);
        when(orderRepository.sumRevenueBetween(any(), any())).thenReturn(new BigDecimal("50000.00"));
        when(orderRepository.findActiveOrders(any())).thenReturn(List.of());
        when(tableRepository.findAll()).thenReturn(List.of());
        when(orderRepository.countByStatusBetween(any(), any())).thenReturn(List.of());
        when(orderRepository.countByOrderTypeBetween(any(), any())).thenReturn(List.of());
        when(paymentRepository.countByPaymentMethodBetween(any(), any())).thenReturn(List.of());
        when(orderRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of());

        // Return 15 items to verify limit of 10
        List<Object[]> fifteenItems = new java.util.ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            fifteenItems.add(new Object[]{(long) i, "Item " + i, (long) (100 - i), new BigDecimal(i * 1000)});
        }
        when(orderRepository.findTopSellingItems(any(), any())).thenReturn(fifteenItems);

        AnalyticsDto.DashboardStats stats = analyticsService.getDashboardStats();

        assertEquals(10, stats.getTopSellingItems().size());
        assertEquals("Item 1", stats.getTopSellingItems().get(0).getItemName());
    }

    @Test
    void getSalesReport_returnsCorrectReport() {
        LocalDate startDate = LocalDate.of(2026, 2, 1);
        LocalDate endDate = LocalDate.of(2026, 2, 10);

        when(orderRepository.dailySalesBetween(any(), any())).thenReturn(List.of(
                new Object[]{"2026-02-01", 10L, new BigDecimal("2500.00")},
                new Object[]{"2026-02-02", 12L, new BigDecimal("3000.00")}
        ));
        when(orderRepository.revenueByCategory(any(), any())).thenReturn(List.of(
                new Object[]{"Main Course", new BigDecimal("3500.00")},
                new Object[]{"Appetizers", new BigDecimal("2000.00")}
        ));
        when(orderRepository.sumRevenueBetween(any(), any())).thenReturn(new BigDecimal("5500.00"));
        when(orderRepository.countByCreatedAtBetween(any(), any())).thenReturn(22L);

        AnalyticsDto.SalesReport report = analyticsService.getSalesReport(startDate, endDate);

        assertEquals(2, report.getDailySales().size());
        assertEquals("2026-02-01", report.getDailySales().get(0).getDate());
        assertEquals(10L, report.getDailySales().get(0).getOrders());
        assertEquals(new BigDecimal("2500.00"), report.getDailySales().get(0).getRevenue());

        assertEquals(2, report.getRevenueByCategory().size());
        assertEquals("Main Course", report.getRevenueByCategory().get(0).getCategory());
        assertEquals(new BigDecimal("3500.00"), report.getRevenueByCategory().get(0).getRevenue());

        assertEquals(new BigDecimal("5500.00"), report.getTotalRevenue());
        assertEquals(22L, report.getTotalOrders());
    }

    @Test
    void getSalesReport_withNoData_returnsEmptyReport() {
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 10);

        when(orderRepository.dailySalesBetween(any(), any())).thenReturn(List.of());
        when(orderRepository.revenueByCategory(any(), any())).thenReturn(List.of());
        when(orderRepository.sumRevenueBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(orderRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);

        AnalyticsDto.SalesReport report = analyticsService.getSalesReport(startDate, endDate);

        assertTrue(report.getDailySales().isEmpty());
        assertTrue(report.getRevenueByCategory().isEmpty());
        assertEquals(BigDecimal.ZERO, report.getTotalRevenue());
        assertEquals(0L, report.getTotalOrders());
    }

    @Test
    void getMenuAnalytics_returnsCorrectAnalytics() {
        when(menuItemRepository.count()).thenReturn(20L);
        when(menuItemRepository.findByAvailableTrue()).thenReturn(List.of(
                MenuItem.builder().id(1L).name("Item 1").available(true).build(),
                MenuItem.builder().id(2L).name("Item 2").available(true).build(),
                MenuItem.builder().id(3L).name("Item 3").available(true).build()
        ));

        List<Object[]> topSellingRaw = List.of(
                new Object[]{1L, "Butter Chicken", 50L, new BigDecimal("17500.00")},
                new Object[]{2L, "Dal Makhani", 30L, new BigDecimal("7500.00")},
                new Object[]{3L, "Naan", 10L, new BigDecimal("500.00")}
        );
        when(orderRepository.findTopSellingItems(any(), any())).thenReturn(topSellingRaw);

        when(orderRepository.revenueByCategory(any(), any())).thenReturn(List.of(
                new Object[]{"Main Course", new BigDecimal("25000.00")},
                new Object[]{"Breads", new BigDecimal("5000.00")}
        ));

        AnalyticsDto.MenuAnalytics analytics = analyticsService.getMenuAnalytics();

        assertEquals(20L, analytics.getTotalItems());
        assertEquals(3L, analytics.getAvailableItems());

        // Top selling: limited to 10 and ordered by quantity DESC (original order)
        assertEquals(3, analytics.getTopSelling().size());
        assertEquals("Butter Chicken", analytics.getTopSelling().get(0).getItemName());
        assertEquals(50L, analytics.getTopSelling().get(0).getQuantitySold());

        // Bottom selling: sorted ascending by quantity
        assertEquals(3, analytics.getBottomSelling().size());
        assertEquals("Naan", analytics.getBottomSelling().get(0).getItemName());
        assertEquals(10L, analytics.getBottomSelling().get(0).getQuantitySold());

        assertEquals(2, analytics.getRevenueByCategory().size());
        assertEquals("Main Course", analytics.getRevenueByCategory().get(0).getCategory());
    }

    @Test
    void getMenuAnalytics_withNoSalesData_returnsEmptyLists() {
        when(menuItemRepository.count()).thenReturn(5L);
        when(menuItemRepository.findByAvailableTrue()).thenReturn(List.of(
                MenuItem.builder().id(1L).name("Item 1").available(true).build()
        ));
        when(orderRepository.findTopSellingItems(any(), any())).thenReturn(List.of());
        when(orderRepository.revenueByCategory(any(), any())).thenReturn(List.of());

        AnalyticsDto.MenuAnalytics analytics = analyticsService.getMenuAnalytics();

        assertEquals(5L, analytics.getTotalItems());
        assertEquals(1L, analytics.getAvailableItems());
        assertTrue(analytics.getTopSelling().isEmpty());
        assertTrue(analytics.getBottomSelling().isEmpty());
        assertTrue(analytics.getRevenueByCategory().isEmpty());
    }

    @Test
    void getMenuAnalytics_topSellingLimitedToTen() {
        when(menuItemRepository.count()).thenReturn(15L);
        when(menuItemRepository.findByAvailableTrue()).thenReturn(List.of());

        List<Object[]> fifteenItems = new java.util.ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            fifteenItems.add(new Object[]{(long) i, "Item " + i, (long) (100 - i), new BigDecimal(i * 500)});
        }
        when(orderRepository.findTopSellingItems(any(), any())).thenReturn(fifteenItems);
        when(orderRepository.revenueByCategory(any(), any())).thenReturn(List.of());

        AnalyticsDto.MenuAnalytics analytics = analyticsService.getMenuAnalytics();

        assertEquals(10, analytics.getTopSelling().size());
        assertEquals("Item 1", analytics.getTopSelling().get(0).getItemName());
        // Bottom selling takes all 15 sorted ascending, limited to 10
        assertEquals(10, analytics.getBottomSelling().size());
        assertEquals("Item 15", analytics.getBottomSelling().get(0).getItemName());
    }
}
