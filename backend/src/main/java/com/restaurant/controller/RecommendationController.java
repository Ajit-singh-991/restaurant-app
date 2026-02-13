package com.restaurant.controller;

import com.restaurant.dto.RecommendationDto.*;
import com.restaurant.security.UserPrincipal;
import com.restaurant.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/personalized")
    public ResponseEntity<PersonalizedRecommendations> getPersonalized(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(recommendationService.getPersonalizedRecommendations(principal.getId()));
    }

    @GetMapping("/trending")
    public ResponseEntity<TrendingItems> getTrending() {
        return ResponseEntity.ok(recommendationService.getTrendingItems());
    }

    @GetMapping("/pairing/{itemId}")
    public ResponseEntity<PairingSuggestion> getPairings(@PathVariable Long itemId) {
        return ResponseEntity.ok(recommendationService.getPairingSuggestions(itemId));
    }
}
