package com.restaurant.service;

import com.restaurant.dto.AdvancedAnalyticsDto.*;
import com.restaurant.entity.MenuItem;
import com.restaurant.entity.Order;
import com.restaurant.entity.OrderItem;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.UserRepository;
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
public class AdvancedAnalyticsService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    public SalesForecast getSalesForecast(int daysAhead) {
        // Use last 30 days to project forward
        LocalDateTime start = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        List<Object[]> dailySales = orderRepository.dailySalesBetween(start, end);

        // Calculate average daily revenue and order count
        BigDecimal totalRev = BigDecimal.ZERO;
        long totalOrders = 0;
        for (Object[] row : dailySales) {
            totalRev = totalRev.add((BigDecimal) row[2]);
            totalOrders += ((Number) row[1]).longValue();
        }

        int dataPoints = Math.max(dailySales.size(), 1);
        BigDecimal avgDailyRevenue = totalRev.divide(BigDecimal.valueOf(dataPoints), 2, RoundingMode.HALF_UP);
        long avgDailyOrders = totalOrders / dataPoints;

        // Simple linear trend: compare first half vs second half
        int half = dataPoints / 2;
        BigDecimal firstHalfAvg = BigDecimal.ZERO;
        BigDecimal secondHalfAvg = BigDecimal.ZERO;
        for (int i = 0; i < dailySales.size(); i++) {
            BigDecimal rev = (BigDecimal) dailySales.get(i)[2];
            if (i < half) firstHalfAvg = firstHalfAvg.add(rev);
            else secondHalfAvg = secondHalfAvg.add(rev);
        }
        if (half > 0) firstHalfAvg = firstHalfAvg.divide(BigDecimal.valueOf(half), 2, RoundingMode.HALF_UP);
        int secondHalfCount = dataPoints - half;
        if (secondHalfCount > 0) secondHalfAvg = secondHalfAvg.divide(BigDecimal.valueOf(secondHalfCount), 2, RoundingMode.HALF_UP);

        double growthTrend = firstHalfAvg.compareTo(BigDecimal.ZERO) > 0
                ? secondHalfAvg.subtract(firstHalfAvg).divide(firstHalfAvg, 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0.0;

        // Generate forecast points
        List<ForecastPoint> forecast = new ArrayList<>();
        for (int i = 1; i <= daysAhead; i++) {
            LocalDate forecastDate = LocalDate.now().plusDays(i);
            BigDecimal dailyGrowthFactor = BigDecimal.ONE.add(
                    BigDecimal.valueOf(growthTrend / 100.0 / 30.0 * i));
            BigDecimal predicted = avgDailyRevenue.multiply(dailyGrowthFactor).setScale(2, RoundingMode.HALF_UP);

            forecast.add(ForecastPoint.builder()
                    .date(forecastDate.toString())
                    .predictedRevenue(predicted)
                    .predictedOrders((long) (avgDailyOrders * dailyGrowthFactor.doubleValue()))
                    .build());
        }

        BigDecimal weeklyProjection = forecast.stream().limit(7)
                .map(ForecastPoint::getPredictedRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthlyProjection = avgDailyRevenue.multiply(BigDecimal.valueOf(30)).setScale(2, RoundingMode.HALF_UP);

        return SalesForecast.builder()
                .forecast(forecast)
                .projectedWeeklyRevenue(weeklyProjection)
                .projectedMonthlyRevenue(monthlyProjection)
                .growthTrendPercent(Math.round(growthTrend * 10.0) / 10.0)
                .build();
    }

    public CustomerSegmentation getCustomerSegmentation() {
        LocalDateTime start = LocalDate.now().minusDays(90).atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        // Group by customer
        Map<Long, List<Order>> byCustomer = orders.stream()
                .filter(o -> o.getCustomer() != null)
                .collect(Collectors.groupingBy(o -> o.getCustomer().getId()));

        List<CustomerSegment> segments = new ArrayList<>();
        long vipCount = 0, regularCount = 0, occasionalCount = 0, newCount = 0;
        BigDecimal vipRev = BigDecimal.ZERO, regRev = BigDecimal.ZERO, occRev = BigDecimal.ZERO, newRev = BigDecimal.ZERO;
        BigDecimal vipAvg = BigDecimal.ZERO, regAvg = BigDecimal.ZERO, occAvg = BigDecimal.ZERO, newAvg = BigDecimal.ZERO;

        for (Map.Entry<Long, List<Order>> entry : byCustomer.entrySet()) {
            List<Order> customerOrders = entry.getValue();
            int orderCount = customerOrders.size();
            BigDecimal totalSpent = customerOrders.stream()
                    .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgOrder = orderCount > 0
                    ? totalSpent.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            if (orderCount >= 10) { vipCount++; vipRev = vipRev.add(totalSpent); vipAvg = vipAvg.add(avgOrder); }
            else if (orderCount >= 5) { regularCount++; regRev = regRev.add(totalSpent); regAvg = regAvg.add(avgOrder); }
            else if (orderCount >= 2) { occasionalCount++; occRev = occRev.add(totalSpent); occAvg = occAvg.add(avgOrder); }
            else { newCount++; newRev = newRev.add(totalSpent); newAvg = newAvg.add(avgOrder); }
        }

        if (vipCount > 0) segments.add(CustomerSegment.builder().segment("VIP").customerCount(vipCount)
                .avgOrderValue(vipAvg.divide(BigDecimal.valueOf(vipCount), 2, RoundingMode.HALF_UP))
                .avgOrderFrequency(10.0).totalRevenue(vipRev).build());
        if (regularCount > 0) segments.add(CustomerSegment.builder().segment("Regular").customerCount(regularCount)
                .avgOrderValue(regAvg.divide(BigDecimal.valueOf(regularCount), 2, RoundingMode.HALF_UP))
                .avgOrderFrequency(5.0).totalRevenue(regRev).build());
        if (occasionalCount > 0) segments.add(CustomerSegment.builder().segment("Occasional").customerCount(occasionalCount)
                .avgOrderValue(occAvg.divide(BigDecimal.valueOf(occasionalCount), 2, RoundingMode.HALF_UP))
                .avgOrderFrequency(2.0).totalRevenue(occRev).build());
        if (newCount > 0) segments.add(CustomerSegment.builder().segment("New").customerCount(newCount)
                .avgOrderValue(newCount > 0 ? newAvg.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .avgOrderFrequency(1.0).totalRevenue(newRev).build());

        return CustomerSegmentation.builder()
                .segments(segments)
                .totalCustomers(byCustomer.size())
                .build();
    }

    public MenuOptimization getMenuOptimization() {
        LocalDateTime start = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        List<Object[]> topSelling = orderRepository.findTopSellingItems(start, end);
        List<MenuItem> allItems = menuItemRepository.findAll();

        // Find items with very low sales
        Set<Long> soldItemIds = topSelling.stream()
                .map(r -> (Long) r[0]).collect(Collectors.toSet());

        List<MenuSuggestion> suggestions = new ArrayList<>();

        // Items never ordered in 30 days
        for (MenuItem item : allItems) {
            if (item.getAvailable() && !soldItemIds.contains(item.getId())) {
                suggestions.add(MenuSuggestion.builder()
                        .itemId(item.getId())
                        .itemName(item.getName())
                        .suggestion("CONSIDER_REMOVING")
                        .reason("No orders in the last 30 days")
                        .priority("HIGH")
                        .build());
            }
        }

        // Items with very low sales (bottom 10% of sold items)
        if (topSelling.size() > 5) {
            long threshold = ((Number) topSelling.get(topSelling.size() - 1)[2]).longValue();
            long lowThreshold = Math.max(threshold, 2);
            for (Object[] row : topSelling) {
                long qty = ((Number) row[2]).longValue();
                if (qty <= lowThreshold) {
                    suggestions.add(MenuSuggestion.builder()
                            .itemId((Long) row[0])
                            .itemName((String) row[1])
                            .suggestion("LOW_PERFORMER")
                            .reason(String.format("Only %d orders in the last 30 days", qty))
                            .priority("MEDIUM")
                            .build());
                }
            }
        }

        // Top items: suggest promotion
        topSelling.stream().limit(3).forEach(row -> {
            suggestions.add(MenuSuggestion.builder()
                    .itemId((Long) row[0])
                    .itemName((String) row[1])
                    .suggestion("PROMOTE")
                    .reason(String.format("Top seller with %d orders — consider featuring or bundling", ((Number) row[2]).longValue()))
                    .priority("LOW")
                    .build());
        });

        return MenuOptimization.builder().suggestions(suggestions).build();
    }

    public PeakHoursAnalysis getPeakHoursAnalysis() {
        LocalDateTime start = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        Map<Integer, List<Order>> byHour = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().getHour()));

        List<HourlyData> hourlyData = new ArrayList<>();
        int peakHour = 0;
        long peakCount = 0;
        int slowestHour = 0;
        long slowestCount = Long.MAX_VALUE;

        for (int h = 0; h < 24; h++) {
            List<Order> hourOrders = byHour.getOrDefault(h, List.of());
            long count = hourOrders.size();
            BigDecimal revenue = hourOrders.stream()
                    .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            hourlyData.add(HourlyData.builder()
                    .hour(h).orderCount(count).revenue(revenue).build());

            if (count > peakCount) { peakCount = count; peakHour = h; }
            if (count < slowestCount) { slowestCount = count; slowestHour = h; }
        }

        return PeakHoursAnalysis.builder()
                .hourlyData(hourlyData)
                .peakHour(peakHour)
                .slowestHour(slowestHour)
                .build();
    }
}
