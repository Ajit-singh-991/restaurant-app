package com.restaurant.service;

import com.restaurant.dto.RecommendationDto.*;
import com.restaurant.entity.Category;
import com.restaurant.entity.MenuItem;
import com.restaurant.entity.Order;
import com.restaurant.entity.OrderItem;
import com.restaurant.entity.User;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private User testCustomer;
    private Category indianCategory;
    private Category drinkCategory;
    private MenuItem butterChicken;
    private MenuItem paneerTikka;
    private MenuItem naanBread;
    private MenuItem mangoLassi;

    @BeforeEach
    void setUp() {
        testCustomer = User.builder()
                .id(1L)
                .username("customer1")
                .fullName("Test Customer")
                .role(User.Role.CUSTOMER)
                .build();

        indianCategory = Category.builder()
                .id(1L)
                .name("Indian")
                .build();

        drinkCategory = Category.builder()
                .id(2L)
                .name("Drinks")
                .build();

        butterChicken = MenuItem.builder()
                .id(5L)
                .name("Butter Chicken")
                .price(new BigDecimal("15.99"))
                .category(indianCategory)
                .available(true)
                .build();

        paneerTikka = MenuItem.builder()
                .id(6L)
                .name("Paneer Tikka")
                .price(new BigDecimal("13.99"))
                .category(indianCategory)
                .available(true)
                .build();

        naanBread = MenuItem.builder()
                .id(7L)
                .name("Naan Bread")
                .price(new BigDecimal("3.99"))
                .category(indianCategory)
                .available(true)
                .build();

        mangoLassi = MenuItem.builder()
                .id(8L)
                .name("Mango Lassi")
                .price(new BigDecimal("5.99"))
                .category(drinkCategory)
                .available(true)
                .build();
    }

    // ---- getPersonalizedRecommendations tests ----

    @Test
    void getPersonalizedRecommendations_withOrderHistory_returnsCollaborativeFiltering() {
        // Customer has ordered Butter Chicken before (from Indian category)
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .menuItem(butterChicken)
                .quantity(2)
                .unitPrice(butterChicken.getPrice())
                .build();

        Order pastOrder = Order.builder()
                .id(20L)
                .orderNumber("ORD-20260213-0001")
                .customer(testCustomer)
                .status(Order.OrderStatus.COMPLETED)
                .items(new ArrayList<>(List.of(orderItem)))
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        // All available items in the system
        when(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(pastOrder));
        when(menuItemRepository.findAll())
                .thenReturn(List.of(butterChicken, paneerTikka, naanBread, mangoLassi));

        PersonalizedRecommendations result =
                recommendationService.getPersonalizedRecommendations(1L);

        assertNotNull(result);
        assertEquals("collaborative_filtering", result.getStrategy());
        assertNotNull(result.getRecommendations());
        // Should recommend Paneer Tikka and Naan (same Indian category, not yet ordered)
        // Should NOT recommend Butter Chicken (already ordered)
        // Should NOT recommend Mango Lassi (different category)
        assertTrue(result.getRecommendations().stream()
                .noneMatch(r -> r.getItemId().equals(5L)));
        assertTrue(result.getRecommendations().stream()
                .anyMatch(r -> r.getItemId().equals(6L)));
        assertTrue(result.getRecommendations().stream()
                .anyMatch(r -> r.getItemId().equals(7L)));
        assertTrue(result.getRecommendations().stream()
                .noneMatch(r -> r.getItemId().equals(8L)));
        // Verify reason mentions the category
        result.getRecommendations().forEach(r ->
                assertTrue(r.getReason().contains("Indian")));
    }

    @Test
    void getPersonalizedRecommendations_withoutOrderHistory_fallsBackToPopular() {
        // No orders for customer
        when(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        Object[] topItem1 = new Object[]{5L, "Butter Chicken", 50L};
        Object[] topItem2 = new Object[]{6L, "Paneer Tikka", 30L};
        when(orderRepository.findTopSellingItems(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(topItem1, topItem2));

        PersonalizedRecommendations result =
                recommendationService.getPersonalizedRecommendations(1L);

        assertNotNull(result);
        assertEquals("popularity_based", result.getStrategy());
        assertNotNull(result.getRecommendations());
        assertEquals(2, result.getRecommendations().size());
        assertEquals(5L, result.getRecommendations().get(0).getItemId());
        assertEquals("Butter Chicken", result.getRecommendations().get(0).getItemName());
        assertEquals("Popular this week", result.getRecommendations().get(0).getReason());
        assertEquals(1.0, result.getRecommendations().get(0).getScore());
    }

    @Test
    void getPersonalizedRecommendations_otherCustomerOrders_ignored() {
        // An order exists, but belongs to a different customer
        User otherCustomer = User.builder()
                .id(99L)
                .username("other")
                .fullName("Other Customer")
                .role(User.Role.CUSTOMER)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .menuItem(butterChicken)
                .quantity(1)
                .unitPrice(butterChicken.getPrice())
                .build();

        Order otherOrder = Order.builder()
                .id(30L)
                .orderNumber("ORD-20260213-0010")
                .customer(otherCustomer)
                .status(Order.OrderStatus.COMPLETED)
                .items(new ArrayList<>(List.of(orderItem)))
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        when(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(otherOrder));

        Object[] topItem = new Object[]{5L, "Butter Chicken", 50L};
        when(orderRepository.findTopSellingItems(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(topItem));

        PersonalizedRecommendations result =
                recommendationService.getPersonalizedRecommendations(1L);

        // Customer 1 has no orders, so should fall back to popularity_based
        assertEquals("popularity_based", result.getStrategy());
    }

    // ---- getTrendingItems tests ----

    @Test
    void getTrendingItems_withGrowth_returnsTrendingSorted() {
        // Current week data
        Object[] current1 = new Object[]{5L, "Butter Chicken", 100L};
        Object[] current2 = new Object[]{6L, "Paneer Tikka", 60L};
        Object[] current3 = new Object[]{7L, "Naan Bread", 20L};

        // Previous week data
        Object[] prev1 = new Object[]{5L, "Butter Chicken", 50L};
        Object[] prev2 = new Object[]{6L, "Paneer Tikka", 60L};

        when(orderRepository.findTopSellingItems(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(current1, current2, current3))
                .thenReturn(Arrays.asList(prev1, prev2));

        TrendingItems result = recommendationService.getTrendingItems();

        assertNotNull(result);
        assertEquals("last_7_days", result.getPeriod());
        assertNotNull(result.getItems());
        assertEquals(3, result.getItems().size());

        // Naan Bread: new item (no previous data) -> 100% growth -> highest
        // Butter Chicken: 100 vs 50 -> 100% growth
        // Paneer Tikka: 60 vs 60 -> 0% growth -> lowest
        assertEquals("Naan Bread", result.getItems().get(0).getItemName());
        assertEquals("Paneer Tikka", result.getItems().get(2).getItemName());
    }

    @Test
    void getTrendingItems_noData_returnsEmptyList() {
        when(orderRepository.findTopSellingItems(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of())
                .thenReturn(List.of());

        TrendingItems result = recommendationService.getTrendingItems();

        assertNotNull(result);
        assertEquals("last_7_days", result.getPeriod());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void getTrendingItems_consistentlyPopular_showsZeroGrowth() {
        Object[] current = new Object[]{5L, "Butter Chicken", 50L};
        Object[] prev = new Object[]{5L, "Butter Chicken", 50L};

        when(orderRepository.findTopSellingItems(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(current))
                .thenReturn(Collections.singletonList(prev));

        TrendingItems result = recommendationService.getTrendingItems();

        assertEquals(1, result.getItems().size());
        assertEquals(0.0, result.getItems().get(0).getScore());
        assertEquals("Consistently popular", result.getItems().get(0).getReason());
    }

    // ---- getPairingSuggestions tests ----

    @Test
    void getPairingSuggestions_withCoOccurringItems_returnsPairings() {
        // Two orders that contain Butter Chicken with other items
        OrderItem bcItem1 = OrderItem.builder().id(1L).menuItem(butterChicken)
                .quantity(1).unitPrice(butterChicken.getPrice()).build();
        OrderItem naanItem1 = OrderItem.builder().id(2L).menuItem(naanBread)
                .quantity(2).unitPrice(naanBread.getPrice()).build();
        OrderItem lassiItem1 = OrderItem.builder().id(3L).menuItem(mangoLassi)
                .quantity(1).unitPrice(mangoLassi.getPrice()).build();

        Order order1 = Order.builder()
                .id(20L)
                .orderNumber("ORD-20260213-0001")
                .customer(testCustomer)
                .status(Order.OrderStatus.COMPLETED)
                .items(new ArrayList<>(List.of(bcItem1, naanItem1, lassiItem1)))
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        OrderItem bcItem2 = OrderItem.builder().id(4L).menuItem(butterChicken)
                .quantity(1).unitPrice(butterChicken.getPrice()).build();
        OrderItem naanItem2 = OrderItem.builder().id(5L).menuItem(naanBread)
                .quantity(1).unitPrice(naanBread.getPrice()).build();

        Order order2 = Order.builder()
                .id(21L)
                .orderNumber("ORD-20260213-0002")
                .customer(testCustomer)
                .status(Order.OrderStatus.COMPLETED)
                .items(new ArrayList<>(List.of(bcItem2, naanItem2)))
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        when(menuItemRepository.findById(5L)).thenReturn(Optional.of(butterChicken));
        when(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(order1, order2));

        PairingSuggestion result = recommendationService.getPairingSuggestions(5L);

        assertNotNull(result);
        assertEquals(5L, result.getItemId());
        assertEquals("Butter Chicken", result.getItemName());
        assertNotNull(result.getPairsWith());
        assertFalse(result.getPairsWith().isEmpty());

        // Naan appears in 2/2 orders -> should be first
        assertEquals("Naan Bread", result.getPairsWith().get(0).getItemName());
        assertEquals("Ordered together 2 times", result.getPairsWith().get(0).getReason());

        // Mango Lassi appears in 1/2 orders
        assertTrue(result.getPairsWith().stream()
                .anyMatch(r -> r.getItemName().equals("Mango Lassi")));
    }

    @Test
    void getPairingSuggestions_itemNotFound_throwsException() {
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> recommendationService.getPairingSuggestions(999L));

        assertEquals("Menu item not found", exception.getMessage());
    }

    @Test
    void getPairingSuggestions_noOrdersWithItem_returnsEmptyPairings() {
        when(menuItemRepository.findById(5L)).thenReturn(Optional.of(butterChicken));
        when(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        PairingSuggestion result = recommendationService.getPairingSuggestions(5L);

        assertNotNull(result);
        assertEquals(5L, result.getItemId());
        assertEquals("Butter Chicken", result.getItemName());
        assertTrue(result.getPairsWith().isEmpty());
    }

    @Test
    void getPairingSuggestions_limitsToFivePairings() {
        // Create an order with the target item and 6 other items
        MenuItem item1 = MenuItem.builder().id(10L).name("Item 1").price(new BigDecimal("10.00"))
                .category(indianCategory).available(true).build();
        MenuItem item2 = MenuItem.builder().id(11L).name("Item 2").price(new BigDecimal("10.00"))
                .category(indianCategory).available(true).build();
        MenuItem item3 = MenuItem.builder().id(12L).name("Item 3").price(new BigDecimal("10.00"))
                .category(indianCategory).available(true).build();
        MenuItem item4 = MenuItem.builder().id(13L).name("Item 4").price(new BigDecimal("10.00"))
                .category(indianCategory).available(true).build();
        MenuItem item5 = MenuItem.builder().id(14L).name("Item 5").price(new BigDecimal("10.00"))
                .category(indianCategory).available(true).build();
        MenuItem item6 = MenuItem.builder().id(15L).name("Item 6").price(new BigDecimal("10.00"))
                .category(indianCategory).available(true).build();

        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder().id(1L).menuItem(butterChicken).quantity(1)
                .unitPrice(butterChicken.getPrice()).build());
        items.add(OrderItem.builder().id(2L).menuItem(item1).quantity(1)
                .unitPrice(item1.getPrice()).build());
        items.add(OrderItem.builder().id(3L).menuItem(item2).quantity(1)
                .unitPrice(item2.getPrice()).build());
        items.add(OrderItem.builder().id(4L).menuItem(item3).quantity(1)
                .unitPrice(item3.getPrice()).build());
        items.add(OrderItem.builder().id(5L).menuItem(item4).quantity(1)
                .unitPrice(item4.getPrice()).build());
        items.add(OrderItem.builder().id(6L).menuItem(item5).quantity(1)
                .unitPrice(item5.getPrice()).build());
        items.add(OrderItem.builder().id(7L).menuItem(item6).quantity(1)
                .unitPrice(item6.getPrice()).build());

        Order order = Order.builder()
                .id(20L)
                .orderNumber("ORD-20260213-0001")
                .customer(testCustomer)
                .status(Order.OrderStatus.COMPLETED)
                .items(items)
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        when(menuItemRepository.findById(5L)).thenReturn(Optional.of(butterChicken));
        when(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(order));

        PairingSuggestion result = recommendationService.getPairingSuggestions(5L);

        assertNotNull(result);
        assertEquals(5, result.getPairsWith().size());
    }
}
