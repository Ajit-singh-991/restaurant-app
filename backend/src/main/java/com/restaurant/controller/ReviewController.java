package com.restaurant.controller;

import com.restaurant.dto.ReviewDto.*;
import com.restaurant.security.UserPrincipal;
import com.restaurant.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateReviewRequest request) {
        return ResponseEntity.ok(reviewService.createReview(principal.getId(), request));
    }

    @GetMapping("/item/{menuItemId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByItem(@PathVariable Long menuItemId) {
        return ResponseEntity.ok(reviewService.getReviewsByMenuItem(menuItemId));
    }

    @GetMapping("/item/{menuItemId}/rating")
    public ResponseEntity<ItemRatingSummary> getItemRating(@PathVariable Long menuItemId) {
        return ResponseEntity.ok(reviewService.getItemRating(menuItemId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reviewService.getReviewsByCustomer(principal.getId()));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(reviewService.getReviewsByOrder(orderId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/unanswered")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ReviewResponse>> getUnansweredReviews() {
        return ResponseEntity.ok(reviewService.getUnansweredReviews());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ReviewStats> getReviewStats() {
        return ResponseEntity.ok(reviewService.getReviewStats());
    }

    @PostMapping("/{id}/respond")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ReviewResponse> respondToReview(
            @PathVariable Long id,
            @RequestBody RespondRequest request) {
        return ResponseEntity.ok(reviewService.respondToReview(id, request.getResponse()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
