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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(Long customerId, CreateReviewRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Can only review completed orders");
        }

        reviewRepository.findByCustomerIdAndOrderId(customerId, request.getOrderId())
                .ifPresent(r -> { throw new IllegalArgumentException("You have already reviewed this order"); });

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Review review = Review.builder()
                .customer(customer)
                .order(order)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        if (request.getMenuItemId() != null) {
            MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
            review.setMenuItem(menuItem);
        }

        Review saved = reviewRepository.save(review);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByMenuItem(Long menuItemId) {
        return reviewRepository.findByMenuItemIdOrderByCreatedAtDesc(menuItemId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByCustomer(Long customerId) {
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByOrder(Long orderId) {
        return reviewRepository.findByOrderId(orderId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemRatingSummary getItemRating(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        Double avg = reviewRepository.findAverageRatingByMenuItemId(menuItemId);
        Long count = reviewRepository.countByMenuItemId(menuItemId);
        return ItemRatingSummary.builder()
                .menuItemId(menuItemId)
                .menuItemName(menuItem.getName())
                .averageRating(avg != null ? avg : 0.0)
                .totalReviews(count != null ? count : 0)
                .build();
    }

    @Transactional
    public ReviewResponse respondToReview(Long reviewId, String response) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        review.setManagementResponse(response);
        review.setRespondedAt(LocalDateTime.now());
        return toResponse(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getUnansweredReviews() {
        return reviewRepository.findUnansweredReviews().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewStats getReviewStats() {
        List<Review> all = reviewRepository.findAll();
        Double avg = reviewRepository.findOverallAverageRating();
        List<ReviewResponse> unanswered = getUnansweredReviews();

        List<RatingBreakdown> breakdown = List.of(1, 2, 3, 4, 5).stream()
                .map(r -> {
                    long count = all.stream().filter(rev -> rev.getRating() == r).count();
                    double pct = all.isEmpty() ? 0 : (count * 100.0 / all.size());
                    return RatingBreakdown.builder()
                            .rating(r)
                            .count(count)
                            .percentage(Math.round(pct * 10.0) / 10.0)
                            .build();
                }).collect(Collectors.toList());

        return ReviewStats.builder()
                .overallRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0)
                .totalReviews(all.size())
                .unansweredCount(unanswered.size())
                .ratingBreakdown(breakdown)
                .build();
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        reviewRepository.delete(review);
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getFullName())
                .orderId(review.getOrder().getId())
                .menuItemId(review.getMenuItem() != null ? review.getMenuItem().getId() : null)
                .menuItemName(review.getMenuItem() != null ? review.getMenuItem().getName() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .managementResponse(review.getManagementResponse())
                .respondedAt(review.getRespondedAt() != null ? review.getRespondedAt().toString() : null)
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
                .build();
    }
}
