package com.restaurant.service;

import com.restaurant.dto.ReviewDto.*;
import com.restaurant.entity.MenuItem;
import com.restaurant.entity.Order;
import com.restaurant.entity.Review;
import com.restaurant.entity.User;
import com.restaurant.repository.MenuItemRepository;
import com.restaurant.repository.OrderRepository;
import com.restaurant.repository.ReviewRepository;
import com.restaurant.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User testCustomer;
    private Order testOrder;
    private MenuItem testMenuItem;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testCustomer = User.builder()
                .id(1L)
                .username("customer1")
                .fullName("Test Customer")
                .role(User.Role.CUSTOMER)
                .build();

        testOrder = Order.builder()
                .id(10L)
                .orderNumber("ORD-20260213-0001")
                .status(Order.OrderStatus.COMPLETED)
                .customer(testCustomer)
                .build();

        testMenuItem = MenuItem.builder()
                .id(5L)
                .name("Butter Chicken")
                .build();

        testReview = Review.builder()
                .id(100L)
                .customer(testCustomer)
                .order(testOrder)
                .menuItem(testMenuItem)
                .rating(4)
                .comment("Great food!")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ---- createReview tests ----

    @Test
    void createReview_success_returnsReviewResponse() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(10L)
                .menuItemId(5L)
                .rating(4)
                .comment("Great food!")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(testOrder));
        when(reviewRepository.findByCustomerIdAndOrderId(1L, 10L)).thenReturn(Optional.empty());
        when(menuItemRepository.findById(5L)).thenReturn(Optional.of(testMenuItem));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        ReviewResponse response = reviewService.createReview(1L, request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getCustomerId());
        assertEquals("Test Customer", response.getCustomerName());
        assertEquals(10L, response.getOrderId());
        assertEquals(5L, response.getMenuItemId());
        assertEquals("Butter Chicken", response.getMenuItemName());
        assertEquals(4, response.getRating());
        assertEquals("Great food!", response.getComment());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_duplicateReview_throwsException() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(10L)
                .rating(4)
                .comment("Another review")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(testOrder));
        when(reviewRepository.findByCustomerIdAndOrderId(1L, 10L)).thenReturn(Optional.of(testReview));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(1L, request));

        assertEquals("You have already reviewed this order", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_invalidRatingTooHigh_throwsException() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(10L)
                .rating(6)
                .comment("Invalid rating")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(testOrder));
        when(reviewRepository.findByCustomerIdAndOrderId(1L, 10L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(1L, request));

        assertEquals("Rating must be between 1 and 5", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_invalidRatingTooLow_throwsException() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(10L)
                .rating(0)
                .comment("Invalid rating")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(testOrder));
        when(reviewRepository.findByCustomerIdAndOrderId(1L, 10L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(1L, request));

        assertEquals("Rating must be between 1 and 5", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_nonCompletedOrder_throwsException() {
        testOrder.setStatus(Order.OrderStatus.PREPARING);

        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(10L)
                .rating(4)
                .comment("Not yet done")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(testOrder));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(1L, request));

        assertEquals("Can only review completed orders", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_userNotFound_throwsException() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(10L)
                .rating(4)
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(999L, request));
    }

    @Test
    void createReview_orderNotFound_throwsException() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(999L)
                .rating(4)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.createReview(1L, request));
    }

    @Test
    void createReview_withoutMenuItem_succeeds() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(10L)
                .menuItemId(null)
                .rating(5)
                .comment("Overall great")
                .build();

        Review reviewWithoutItem = Review.builder()
                .id(101L)
                .customer(testCustomer)
                .order(testOrder)
                .menuItem(null)
                .rating(5)
                .comment("Overall great")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(testOrder));
        when(reviewRepository.findByCustomerIdAndOrderId(1L, 10L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewWithoutItem);

        ReviewResponse response = reviewService.createReview(1L, request);

        assertNotNull(response);
        assertNull(response.getMenuItemId());
        assertNull(response.getMenuItemName());
        verify(menuItemRepository, never()).findById(any());
    }

    // ---- getReviewsByMenuItem tests ----

    @Test
    void getReviewsByMenuItem_returnsListOfReviews() {
        Review review2 = Review.builder()
                .id(102L)
                .customer(testCustomer)
                .order(testOrder)
                .menuItem(testMenuItem)
                .rating(5)
                .comment("Excellent!")
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewRepository.findByMenuItemIdOrderByCreatedAtDesc(5L))
                .thenReturn(List.of(testReview, review2));

        List<ReviewResponse> result = reviewService.getReviewsByMenuItem(5L);

        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals(102L, result.get(1).getId());
    }

    @Test
    void getReviewsByMenuItem_noReviews_returnsEmptyList() {
        when(reviewRepository.findByMenuItemIdOrderByCreatedAtDesc(99L))
                .thenReturn(List.of());

        List<ReviewResponse> result = reviewService.getReviewsByMenuItem(99L);

        assertTrue(result.isEmpty());
    }

    // ---- respondToReview tests ----

    @Test
    void respondToReview_success_setsManagementResponse() {
        Review savedReview = Review.builder()
                .id(100L)
                .customer(testCustomer)
                .order(testOrder)
                .menuItem(testMenuItem)
                .rating(4)
                .comment("Great food!")
                .managementResponse("Thank you for your feedback!")
                .respondedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewRepository.findById(100L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponse response = reviewService.respondToReview(100L, "Thank you for your feedback!");

        assertNotNull(response);
        assertEquals("Thank you for your feedback!", response.getManagementResponse());
        assertNotNull(response.getRespondedAt());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void respondToReview_reviewNotFound_throwsException() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.respondToReview(999L, "Some response"));
    }

    // ---- getReviewStats tests ----

    @Test
    void getReviewStats_withReviews_returnsCorrectStats() {
        Review review1 = Review.builder().id(1L).customer(testCustomer).order(testOrder)
                .rating(5).createdAt(LocalDateTime.now()).build();
        Review review2 = Review.builder().id(2L).customer(testCustomer).order(testOrder)
                .rating(4).createdAt(LocalDateTime.now()).build();
        Review review3 = Review.builder().id(3L).customer(testCustomer).order(testOrder)
                .rating(4).managementResponse(null).createdAt(LocalDateTime.now()).build();

        when(reviewRepository.findAll()).thenReturn(List.of(review1, review2, review3));
        when(reviewRepository.findOverallAverageRating()).thenReturn(4.333);
        when(reviewRepository.findUnansweredReviews()).thenReturn(List.of(review1, review2, review3));

        ReviewStats stats = reviewService.getReviewStats();

        assertEquals(4.3, stats.getOverallRating());
        assertEquals(3, stats.getTotalReviews());
        assertEquals(3, stats.getUnansweredCount());
        assertNotNull(stats.getRatingBreakdown());
        assertEquals(5, stats.getRatingBreakdown().size());
    }

    @Test
    void getReviewStats_noReviews_returnsZeroStats() {
        when(reviewRepository.findAll()).thenReturn(List.of());
        when(reviewRepository.findOverallAverageRating()).thenReturn(null);
        when(reviewRepository.findUnansweredReviews()).thenReturn(List.of());

        ReviewStats stats = reviewService.getReviewStats();

        assertEquals(0.0, stats.getOverallRating());
        assertEquals(0, stats.getTotalReviews());
        assertEquals(0, stats.getUnansweredCount());
    }

    @Test
    void getReviewStats_ratingBreakdown_calculatesCorrectPercentages() {
        Review r1 = Review.builder().id(1L).customer(testCustomer).order(testOrder)
                .rating(5).createdAt(LocalDateTime.now()).build();
        Review r2 = Review.builder().id(2L).customer(testCustomer).order(testOrder)
                .rating(5).createdAt(LocalDateTime.now()).build();
        Review r3 = Review.builder().id(3L).customer(testCustomer).order(testOrder)
                .rating(3).createdAt(LocalDateTime.now()).build();
        Review r4 = Review.builder().id(4L).customer(testCustomer).order(testOrder)
                .rating(1).createdAt(LocalDateTime.now()).build();

        when(reviewRepository.findAll()).thenReturn(List.of(r1, r2, r3, r4));
        when(reviewRepository.findOverallAverageRating()).thenReturn(3.5);
        when(reviewRepository.findUnansweredReviews()).thenReturn(List.of());

        ReviewStats stats = reviewService.getReviewStats();

        List<RatingBreakdown> breakdown = stats.getRatingBreakdown();
        // rating=1: 1 out of 4 = 25%
        assertEquals(1, breakdown.get(0).getCount());
        assertEquals(25.0, breakdown.get(0).getPercentage());
        // rating=2: 0 out of 4 = 0%
        assertEquals(0, breakdown.get(1).getCount());
        assertEquals(0.0, breakdown.get(1).getPercentage());
        // rating=3: 1 out of 4 = 25%
        assertEquals(1, breakdown.get(2).getCount());
        assertEquals(25.0, breakdown.get(2).getPercentage());
        // rating=4: 0 out of 4 = 0%
        assertEquals(0, breakdown.get(3).getCount());
        assertEquals(0.0, breakdown.get(3).getPercentage());
        // rating=5: 2 out of 4 = 50%
        assertEquals(2, breakdown.get(4).getCount());
        assertEquals(50.0, breakdown.get(4).getPercentage());
    }

    // ---- deleteReview tests ----

    @Test
    void deleteReview_success_deletesReview() {
        when(reviewRepository.findById(100L)).thenReturn(Optional.of(testReview));

        reviewService.deleteReview(100L);

        verify(reviewRepository).delete(testReview);
    }

    @Test
    void deleteReview_reviewNotFound_throwsException() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> reviewService.deleteReview(999L));

        verify(reviewRepository, never()).delete(any());
    }
}
