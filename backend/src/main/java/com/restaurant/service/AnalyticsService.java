package com.restaurant.service;

import com.restaurant.dto.AnalyticsDto;
import com.restaurant.entity.Order;
import com.restaurant.entity.RestaurantTable;
import com.restaurant.entity.User;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.PaymentRepository;
import com.restaurant.repository.TableRepository;
import com.restaurant.repository.UserRepository;
import com.restaurant.repository.TimeEntryRepository;
import com.restaurant.entity.TimeEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final MenuItemRepository menuItemRepository;
    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    private final TimeEntryRepository timeEntryRepository;

    public AnalyticsDto.DashboardStats getDashboardStats() {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(LocalTime.MAX);

        long totalOrders = orderRepository.countByCreatedAtBetween(dayStart, dayEnd);
        BigDecimal totalRevenue = orderRepository.sumRevenueBetween(dayStart, dayEnd);
        BigDecimal avgOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long activeOrders = orderRepository.findActiveOrders(List.of(
                Order.OrderStatus.PENDING, Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.PREPARING, Order.OrderStatus.READY
        )).size();

        List<RestaurantTable> allTables = tableRepository.findAll();
        int tablesOccupied = (int) allTables.stream()
                .filter(t -> t.getStatus() == RestaurantTable.TableStatus.OCCUPIED).count();

        Map<String, Long> ordersByStatus = orderRepository.countByStatusBetween(dayStart, dayEnd)
                .stream().collect(Collectors.toMap(
                        r -> r[0].toString(), r -> (Long) r[1], (a, b) -> a, LinkedHashMap::new));

        Map<String, Long> ordersByType = orderRepository.countByOrderTypeBetween(dayStart, dayEnd)
                .stream().collect(Collectors.toMap(
                        r -> r[0].toString(), r -> (Long) r[1], (a, b) -> a, LinkedHashMap::new));

        Map<String, Long> ordersByPaymentMethod = paymentRepository.countByPaymentMethodBetween(dayStart, dayEnd)
                .stream().collect(Collectors.toMap(
                        r -> r[0].toString(), r -> ((Number) r[1]).longValue(), (a, b) -> a, LinkedHashMap::new));

        List<Order> recentOrderList = orderRepository.findTop10ByOrderByCreatedAtDesc();
        List<AnalyticsDto.RecentOrderSummary> recentOrders = recentOrderList.stream()
                .map(o -> AnalyticsDto.RecentOrderSummary.builder()
                        .id(o.getId())
                        .orderNumber(o.getOrderNumber())
                        .status(o.getStatus().toString())
                        .totalAmount(o.getTotalAmount())
                        .createdAt(o.getCreatedAt())
                        .build())
                .toList();

        List<AnalyticsDto.TopItem> topItems = orderRepository.findTopSellingItems(dayStart, dayEnd)
                .stream().limit(10).map(r -> AnalyticsDto.TopItem.builder()
                        .itemId((Long) r[0])
                        .itemName((String) r[1])
                        .quantitySold(((Number) r[2]).longValue())
                        .revenue((BigDecimal) r[3])
                        .build()).toList();

        return AnalyticsDto.DashboardStats.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .averageOrderValue(avgOrderValue)
                .activeOrders(activeOrders)
                .tablesOccupied(tablesOccupied)
                .totalTables(allTables.size())
                .ordersByStatus(ordersByStatus)
                .ordersByType(ordersByType)
                .ordersByPaymentMethod(ordersByPaymentMethod != null ? ordersByPaymentMethod : Map.of())
                .topSellingItems(topItems)
                .recentOrders(recentOrders != null ? recentOrders : List.of())
                .build();
    }

    public AnalyticsDto.SalesReport getSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<AnalyticsDto.DailySales> dailySales = orderRepository.dailySalesBetween(start, end)
                .stream().map(r -> AnalyticsDto.DailySales.builder()
                        .date(r[0].toString())
                        .orders(((Number) r[1]).longValue())
                        .revenue((BigDecimal) r[2])
                        .build()).toList();

        List<AnalyticsDto.CategoryRevenue> revenueByCategory = orderRepository.revenueByCategory(start, end)
                .stream().map(r -> AnalyticsDto.CategoryRevenue.builder()
                        .category((String) r[0])
                        .revenue((BigDecimal) r[1])
                        .build()).toList();

        BigDecimal totalRevenue = orderRepository.sumRevenueBetween(start, end);
        long totalOrders = orderRepository.countByCreatedAtBetween(start, end);

        return AnalyticsDto.SalesReport.builder()
                .dailySales(dailySales)
                .revenueByCategory(revenueByCategory)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .build();
    }

    public AnalyticsDto.MenuAnalytics getMenuAnalytics() {
        LocalDateTime start = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        long totalItems = menuItemRepository.count();
        long availableItems = menuItemRepository.findByAvailableTrue().size();

        List<Object[]> topRaw = orderRepository.findTopSellingItems(start, end);
        List<AnalyticsDto.TopItem> topSelling = topRaw.stream().limit(10).map(r ->
                AnalyticsDto.TopItem.builder()
                        .itemId((Long) r[0]).itemName((String) r[1])
                        .quantitySold(((Number) r[2]).longValue()).revenue((BigDecimal) r[3])
                        .build()).toList();

        // Bottom selling: reverse order, take last items with lowest quantity
        List<AnalyticsDto.TopItem> bottomSelling = topRaw.stream()
                .sorted((a, b) -> Long.compare(((Number) a[2]).longValue(), ((Number) b[2]).longValue()))
                .limit(10).map(r -> AnalyticsDto.TopItem.builder()
                        .itemId((Long) r[0]).itemName((String) r[1])
                        .quantitySold(((Number) r[2]).longValue()).revenue((BigDecimal) r[3])
                        .build()).toList();

        List<AnalyticsDto.CategoryRevenue> revByCat = orderRepository.revenueByCategory(start, end)
                .stream().map(r -> AnalyticsDto.CategoryRevenue.builder()
                        .category((String) r[0]).revenue((BigDecimal) r[1]).build()).toList();

        return AnalyticsDto.MenuAnalytics.builder()
                .totalItems(totalItems)
                .availableItems(availableItems)
                .topSelling(topSelling)
                .bottomSelling(bottomSelling)
                .revenueByCategory(revByCat)
                .build();
    }

    public AnalyticsDto.CustomerAnalytics getCustomerAnalytics() {
        LocalDateTime monthStart = LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        long totalCustomers = userRepository.countByRole(User.Role.CUSTOMER);
        long newCustomersThisMonth = userRepository.countByRoleAndCreatedAtAfter(User.Role.CUSTOMER, monthStart);

        List<Object[]> ordersByCustomer = orderRepository.countOrdersByCustomerBetween(monthStart, now);
        long repeatCustomers = ordersByCustomer.stream().filter(r -> ((Number) r[1]).longValue() > 1).count();
        long totalOrdersInPeriod = ordersByCustomer.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        long customersWithOrders = ordersByCustomer.size();
        double avgOrders = customersWithOrders > 0 ? (double) totalOrdersInPeriod / customersWithOrders : 0;

        return AnalyticsDto.CustomerAnalytics.builder()
                .totalCustomers(totalCustomers)
                .newCustomersThisMonth(newCustomersThisMonth)
                .repeatCustomers(repeatCustomers)
                .averageOrdersPerCustomer(avgOrders)
                .build();
    }

    public AnalyticsDto.StaffPerformance getStaffPerformance() {
        LocalDateTime monthStart = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<Object[]> orderCounts = orderRepository.countOrdersByWaiterBetween(monthStart, now);
        List<Object[]> tipsSums = timeEntryRepository.sumTipsByStaffBetween(monthStart, now);
        Map<Long, BigDecimal> tipsByStaff = tipsSums.stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (BigDecimal) r[1], (a, b) -> a));

        List<AnalyticsDto.StaffPerformanceEntry> entries = new ArrayList<>();
        for (Object[] row : orderCounts) {
            Long staffId = (Long) row[0];
            long ordersHandled = ((Number) row[1]).longValue();
            BigDecimal totalTips = tipsByStaff.getOrDefault(staffId, BigDecimal.ZERO);
            String staffName = userRepository.findById(staffId).map(User::getUsername).orElse("Unknown");
            entries.add(AnalyticsDto.StaffPerformanceEntry.builder()
                    .staffId(staffId)
                    .staffName(staffName)
                    .ordersHandled(ordersHandled)
                    .totalTips(totalTips)
                    .build());
        }

        return AnalyticsDto.StaffPerformance.builder().entries(entries).build();
    }

    public AnalyticsDto.InventoryReport getInventoryReport() {
        // Stub: no Ingredient entity in codebase; return empty report
        return AnalyticsDto.InventoryReport.builder()
                .lowStockItems(Collections.emptyList())
                .totalStockValue(BigDecimal.ZERO)
                .build();
    }

    public byte[] exportReport(AnalyticsDto.ReportType type, AnalyticsDto.ExportFormat format,
                               LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        return switch (format) {
            case CSV -> exportCsv(type, start, end);
            case PDF -> exportPdf(type, start, end);
            case EXCEL -> exportExcel(type, start, end);
        };
    }

    private byte[] exportCsv(AnalyticsDto.ReportType type, LocalDateTime start, LocalDateTime end) {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case SALES -> {
                AnalyticsDto.SalesReport report = getSalesReport(start.toLocalDate(), end.toLocalDate());
                sb.append("date,orders,revenue\n");
                for (AnalyticsDto.DailySales d : report.getDailySales()) {
                    sb.append(d.getDate()).append(",").append(d.getOrders()).append(",")
                            .append(d.getRevenue()).append("\n");
                }
                sb.append("Total,,").append(report.getTotalOrders()).append(",").append(report.getTotalRevenue()).append("\n");
            }
            case MENU -> {
                AnalyticsDto.MenuAnalytics report = getMenuAnalytics();
                sb.append("itemId,itemName,quantitySold,revenue\n");
                for (AnalyticsDto.TopItem t : report.getTopSelling()) {
                    sb.append(t.getItemId()).append(",").append(t.getItemName()).append(",")
                            .append(t.getQuantitySold()).append(",").append(t.getRevenue()).append("\n");
                }
            }
            case CUSTOMERS -> {
                AnalyticsDto.CustomerAnalytics report = getCustomerAnalytics();
                sb.append("metric,value\n");
                sb.append("totalCustomers,").append(report.getTotalCustomers()).append("\n");
                sb.append("newCustomersThisMonth,").append(report.getNewCustomersThisMonth()).append("\n");
                sb.append("repeatCustomers,").append(report.getRepeatCustomers()).append("\n");
                sb.append("averageOrdersPerCustomer,").append(report.getAverageOrdersPerCustomer()).append("\n");
            }
            case STAFF -> {
                AnalyticsDto.StaffPerformance report = getStaffPerformance();
                sb.append("staffId,staffName,ordersHandled,totalTips\n");
                for (AnalyticsDto.StaffPerformanceEntry e : report.getEntries()) {
                    sb.append(e.getStaffId()).append(",").append(e.getStaffName()).append(",")
                            .append(e.getOrdersHandled()).append(",").append(e.getTotalTips()).append("\n");
                }
            }
            case INVENTORY -> sb.append("No inventory data\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] exportPdf(AnalyticsDto.ReportType type, LocalDateTime start, LocalDateTime end) {
        try {
            com.lowagie.text.Document document = new com.lowagie.text.Document();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            com.lowagie.text.pdf.PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new com.lowagie.text.Paragraph("Analytics Report: " + type + " (" + start.toLocalDate() + " to " + end.toLocalDate() + ")"));
            document.add(new com.lowagie.text.Paragraph(" "));
            String content = switch (type) {
                case SALES -> "Total revenue in period: " + orderRepository.sumRevenueBetween(start, end);
                case MENU -> "Menu analytics - see dashboard for details.";
                case CUSTOMERS -> "Customer analytics - see dashboard for details.";
                case STAFF -> "Staff performance - see dashboard for details.";
                case INVENTORY -> "No inventory data.";
            };
            document.add(new com.lowagie.text.Paragraph(content));
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF export failed", e);
        }
    }

    private byte[] exportExcel(AnalyticsDto.ReportType type, LocalDateTime start, LocalDateTime end) {
        // Simple CSV as fallback when POI not added to avoid new dependency; or add Apache POI
        return exportCsv(type, start, end);
    }
}
