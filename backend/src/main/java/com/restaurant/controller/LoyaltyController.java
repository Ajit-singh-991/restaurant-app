package com.restaurant.controller;

import com.restaurant.dto.LoyaltyDto.*;
import com.restaurant.security.UserPrincipal;
import com.restaurant.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping("/my")
    public ResponseEntity<LoyaltyAccountResponse> getMyAccount(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(loyaltyService.getOrCreateAccount(principal.getId()));
    }

    @GetMapping("/my/transactions")
    public ResponseEntity<List<LoyaltyTransactionResponse>> getMyTransactions(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(loyaltyService.getTransactionHistory(principal.getId()));
    }

    @PostMapping("/redeem")
    public ResponseEntity<LoyaltyAccountResponse> redeemPoints(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody RedeemRequest request) {
        return ResponseEntity.ok(loyaltyService.redeemPoints(principal.getId(), request.getPoints()));
    }

    @GetMapping("/accounts")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<LoyaltyAccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(loyaltyService.getAllAccounts());
    }
}
