package com.restaurant.service;

import com.restaurant.dto.RecommendationDto.*;
import com.restaurant.entity.MenuItem;
import com.restaurant.entity.Order;
import com.restaurant.entity.OrderItem;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderRepository;
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
public class RecommendationService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;

    public PersonalizedRecommendations getPersonalizedRecommendations(Long customerId) {
        LocalDateTime start = LocalDate.now().minusDays(90).atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        List<Order> customerOrders = orderRepository.findByCreatedAtBetween(start, end).stream()
                .filter(o -> o.getCustomer() != null && o.getCustomer().getId().equals(customerId))
                .collect(Collectors.toList());

        if (customerOrders.isEmpty()) {
            return getPopularRecommendations();
        }

        // Find categories and items the customer frequently orders
        Map<Long, Long> itemFrequency = new HashMap<>();
        Set<Long> orderedCategories = new HashSet<>();

        for (Order order : customerOrders) {
            for (OrderItem item : order.getItems()) {
                itemFrequency.merge(item.getMenuItem().getId(), (long) item.getQuantity(), Long::sum);
                if (item.getMenuItem().getCategory() != null) {
                    orderedCategories.add(item.getMenuItem().getCategory().getId());
                }
            }
        }

        // Recommend items from same categories that they haven't tried
        List<MenuItem> allItems = menuItemRepository.findAll().stream()
                .filter(MenuItem::getAvailable)
                .collect(Collectors.toList());

        List<RecommendedItem> recommendations = allItems.stream()
                .filter(item -> !itemFrequency.containsKey(item.getId()))
                .filter(item -> item.getCategory() != null && orderedCategories.contains(item.getCategory().getId()))
                .map(item -> RecommendedItem.builder()
                        .itemId(item.getId())
                        .itemName(item.getName())
                        .category(item.getCategory() != null ? item.getCategory().getName() : "Uncategorized")
                        .price(item.getPrice())
                        .score(0.8)
                        .reason("Based on your preference for " + (item.getCategory() != null ? item.getCategory().getName() : "this category"))
                        .build())
                .limit(10)
                .collect(Collectors.toList());

        return PersonalizedRecommendations.builder()
                .recommendations(recommendations)
                .strategy("collaborative_filtering")
                .build();
    }

    private PersonalizedRecommendations getPopularRecommendations() {
        LocalDateTime start = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        List<Object[]> topSelling = orderRepository.findTopSellingItems(start, end);

        List<RecommendedItem> recommendations = topSelling.stream()
                .limit(10)
                .map(row -> RecommendedItem.builder()
                        .itemId((Long) row[0])
                        .itemName((String) row[1])
                        .score(1.0)
                        .reason("Popular this week")
                        .build())
                .collect(Collectors.toList());

        return PersonalizedRecommendations.builder()
                .recommendations(recommendations)
                .strategy("popularity_based")
                .build();
    }

    public TrendingItems getTrendingItems() {
        LocalDateTime start = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        List<Object[]> topSelling = orderRepository.findTopSellingItems(start, end);

        // Compare with previous week to find trending
        LocalDateTime prevStart = LocalDate.now().minusDays(14).atStartOfDay();
        LocalDateTime prevEnd = LocalDate.now().minusDays(7).atStartOfDay();

        List<Object[]> prevSelling = orderRepository.findTopSellingItems(prevStart, prevEnd);
        Map<Long, Long> prevQty = prevSelling.stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> ((Number) r[2]).longValue(), Long::sum));

        List<RecommendedItem> trending = topSelling.stream()
                .map(row -> {
                    Long itemId = (Long) row[0];
                    long currentQty = ((Number) row[2]).longValue();
                    long previousQty = prevQty.getOrDefault(itemId, 0L);
                    double growth = previousQty > 0
                            ? ((double) (currentQty - previousQty) / previousQty) * 100
                            : 100.0;

                    return RecommendedItem.builder()
                            .itemId(itemId)
                            .itemName((String) row[1])
                            .score(Math.max(0, growth))
                            .reason(growth > 0
                                    ? String.format("%.0f%% increase from last week", growth)
                                    : "Consistently popular")
                            .build();
                })
                .sorted(Comparator.comparingDouble(RecommendedItem::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return TrendingItems.builder()
                .items(trending)
                .period("last_7_days")
                .build();
    }

    public PairingSuggestion getPairingSuggestions(Long itemId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        LocalDateTime start = LocalDate.now().minusDays(60).atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        // Find orders that contain this item
        List<Order> ordersWithItem = orderRepository.findByCreatedAtBetween(start, end).stream()
                .filter(o -> o.getItems().stream().anyMatch(oi -> oi.getMenuItem().getId().equals(itemId)))
                .collect(Collectors.toList());

        // Count co-occurring items
        Map<Long, Long> coOccurrence = new HashMap<>();
        Map<Long, MenuItem> itemMap = new HashMap<>();
        for (Order order : ordersWithItem) {
            for (OrderItem oi : order.getItems()) {
                if (!oi.getMenuItem().getId().equals(itemId)) {
                    coOccurrence.merge(oi.getMenuItem().getId(), 1L, Long::sum);
                    itemMap.put(oi.getMenuItem().getId(), oi.getMenuItem());
                }
            }
        }

        List<RecommendedItem> pairings = coOccurrence.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    MenuItem pairedItem = itemMap.get(entry.getKey());
                    double score = ordersWithItem.isEmpty() ? 0 :
                            BigDecimal.valueOf(entry.getValue())
                                    .divide(BigDecimal.valueOf(ordersWithItem.size()), 2, RoundingMode.HALF_UP)
                                    .doubleValue();
                    return RecommendedItem.builder()
                            .itemId(pairedItem.getId())
                            .itemName(pairedItem.getName())
                            .category(pairedItem.getCategory() != null ? pairedItem.getCategory().getName() : null)
                            .price(pairedItem.getPrice())
                            .score(score)
                            .reason(String.format("Ordered together %d times", entry.getValue()))
                            .build();
                })
                .collect(Collectors.toList());

        return PairingSuggestion.builder()
                .itemId(item.getId())
                .itemName(item.getName())
                .pairsWith(pairings)
                .build();
    }
}
