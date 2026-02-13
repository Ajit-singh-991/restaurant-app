package com.restaurant.service;

import com.restaurant.dto.LoyaltyDto.*;
import com.restaurant.entity.LoyaltyAccount;
import com.restaurant.entity.LoyaltyAccount.Tier;
import com.restaurant.entity.LoyaltyTransaction;
import com.restaurant.entity.LoyaltyTransaction.TransactionType;
import com.restaurant.entity.Order;
import com.restaurant.entity.User;
import com.restaurant.repository.LoyaltyAccountRepository;
import com.restaurant.repository.LoyaltyTransactionRepository;
import com.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // Points earned per dollar spent
    private static final int POINTS_PER_DOLLAR = 10;
    // Tier thresholds (total points earned)
    private static final int SILVER_THRESHOLD = 500;
    private static final int GOLD_THRESHOLD = 2000;
    private static final int PLATINUM_THRESHOLD = 5000;

    @Transactional
    public LoyaltyAccountResponse getOrCreateAccount(Long customerId) {
        LoyaltyAccount account = accountRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    User customer = userRepository.findById(customerId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));
                    return accountRepository.save(LoyaltyAccount.builder()
                            .customer(customer)
                            .build());
                });
        return toAccountResponse(account);
    }

    @Transactional
    public LoyaltyAccountResponse earnPoints(Long customerId, Order order) {
        LoyaltyAccount account = accountRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    User customer = userRepository.findById(customerId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found"));
                    return accountRepository.save(LoyaltyAccount.builder()
                            .customer(customer)
                            .build());
                });

        int earned = order.getTotalAmount().intValue() * POINTS_PER_DOLLAR;

        // Tier multiplier
        double multiplier = switch (account.getTier()) {
            case SILVER -> 1.25;
            case GOLD -> 1.5;
            case PLATINUM -> 2.0;
            default -> 1.0;
        };
        earned = (int) (earned * multiplier);

        account.setPoints(account.getPoints() + earned);
        account.setTotalPointsEarned(account.getTotalPointsEarned() + earned);
        updateTier(account);
        accountRepository.save(account);

        transactionRepository.save(LoyaltyTransaction.builder()
                .account(account)
                .order(order)
                .type(TransactionType.EARNED)
                .points(earned)
                .description(String.format("Earned from order %s (%.1fx multiplier)", order.getOrderNumber(), multiplier))
                .build());

        return toAccountResponse(account);
    }

    @Transactional
    public LoyaltyAccountResponse redeemPoints(Long customerId, int points) {
        LoyaltyAccount account = accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Loyalty account not found"));

        if (account.getPoints() < points) {
            throw new IllegalArgumentException("Insufficient points. Available: " + account.getPoints());
        }
        if (points <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }

        account.setPoints(account.getPoints() - points);
        accountRepository.save(account);

        transactionRepository.save(LoyaltyTransaction.builder()
                .account(account)
                .type(TransactionType.REDEEMED)
                .points(-points)
                .description("Points redeemed for discount")
                .build());

        return toAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public List<LoyaltyTransactionResponse> getTransactionHistory(Long customerId) {
        return transactionRepository.findByAccountCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoyaltyAccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toAccountResponse)
                .collect(Collectors.toList());
    }

    private void updateTier(LoyaltyAccount account) {
        int total = account.getTotalPointsEarned();
        if (total >= PLATINUM_THRESHOLD) {
            account.setTier(Tier.PLATINUM);
        } else if (total >= GOLD_THRESHOLD) {
            account.setTier(Tier.GOLD);
        } else if (total >= SILVER_THRESHOLD) {
            account.setTier(Tier.SILVER);
        } else {
            account.setTier(Tier.BRONZE);
        }
    }

    private int getPointsToNextTier(LoyaltyAccount account) {
        int total = account.getTotalPointsEarned();
        return switch (account.getTier()) {
            case BRONZE -> SILVER_THRESHOLD - total;
            case SILVER -> GOLD_THRESHOLD - total;
            case GOLD -> PLATINUM_THRESHOLD - total;
            case PLATINUM -> 0;
        };
    }

    private LoyaltyAccountResponse toAccountResponse(LoyaltyAccount account) {
        return LoyaltyAccountResponse.builder()
                .id(account.getId())
                .customerId(account.getCustomer().getId())
                .customerName(account.getCustomer().getFullName())
                .points(account.getPoints())
                .totalPointsEarned(account.getTotalPointsEarned())
                .tier(account.getTier().name())
                .pointsToNextTier(getPointsToNextTier(account))
                .createdAt(account.getCreatedAt() != null ? account.getCreatedAt().toString() : null)
                .build();
    }

    private LoyaltyTransactionResponse toTransactionResponse(LoyaltyTransaction tx) {
        return LoyaltyTransactionResponse.builder()
                .id(tx.getId())
                .type(tx.getType().name())
                .points(tx.getPoints())
                .description(tx.getDescription())
                .orderId(tx.getOrder() != null ? tx.getOrder().getId() : null)
                .createdAt(tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : null)
                .build();
    }
}
