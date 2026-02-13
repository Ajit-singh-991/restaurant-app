package com.restaurant.service;

import com.restaurant.dto.LoyaltyDto.*;
import com.restaurant.entity.LoyaltyAccount;
import com.restaurant.entity.LoyaltyAccount.Tier;
import com.restaurant.entity.LoyaltyTransaction;
import com.restaurant.entity.Order;
import com.restaurant.entity.User;
import com.restaurant.repository.LoyaltyAccountRepository;
import com.restaurant.repository.LoyaltyTransactionRepository;
import com.restaurant.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {

    @Mock
    private LoyaltyAccountRepository accountRepository;

    @Mock
    private LoyaltyTransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoyaltyService loyaltyService;

    private User testCustomer;
    private LoyaltyAccount testAccount;

    @BeforeEach
    void setUp() {
        testCustomer = User.builder()
                .id(1L)
                .username("customer1")
                .fullName("Test Customer")
                .role(User.Role.CUSTOMER)
                .build();

        testAccount = LoyaltyAccount.builder()
                .id(10L)
                .customer(testCustomer)
                .points(100)
                .totalPointsEarned(100)
                .tier(Tier.BRONZE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ---- getOrCreateAccount tests ----

    @Test
    void getOrCreateAccount_existingAccount_returnsExistingAccount() {
        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));

        LoyaltyAccountResponse response = loyaltyService.getOrCreateAccount(1L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getCustomerId());
        assertEquals("Test Customer", response.getCustomerName());
        assertEquals(100, response.getPoints());
        assertEquals(100, response.getTotalPointsEarned());
        assertEquals("BRONZE", response.getTier());
        verify(accountRepository, never()).save(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getOrCreateAccount_newAccount_createsAndReturnsAccount() {
        LoyaltyAccount newAccount = LoyaltyAccount.builder()
                .id(11L)
                .customer(testCustomer)
                .points(0)
                .totalPointsEarned(0)
                .tier(Tier.BRONZE)
                .createdAt(LocalDateTime.now())
                .build();

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenReturn(newAccount);

        LoyaltyAccountResponse response = loyaltyService.getOrCreateAccount(1L);

        assertNotNull(response);
        assertEquals(11L, response.getId());
        assertEquals(0, response.getPoints());
        assertEquals(0, response.getTotalPointsEarned());
        assertEquals("BRONZE", response.getTier());
        verify(accountRepository).save(any(LoyaltyAccount.class));
    }

    @Test
    void getOrCreateAccount_userNotFound_throwsException() {
        when(accountRepository.findByCustomerId(999L)).thenReturn(Optional.empty());
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> loyaltyService.getOrCreateAccount(999L));
    }

    // ---- earnPoints tests ----

    @Test
    void earnPoints_bronzeTier_earnsBasePoints() {
        Order order = Order.builder()
                .id(20L)
                .orderNumber("ORD-20260213-0001")
                .totalAmount(new BigDecimal("100.00"))
                .build();

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyAccountResponse response = loyaltyService.earnPoints(1L, order);

        // 100 * 10 = 1000 points, 1.0x multiplier for BRONZE
        assertEquals(1100, response.getPoints()); // 100 existing + 1000 earned
        assertEquals(1100, response.getTotalPointsEarned());
        verify(transactionRepository).save(any(LoyaltyTransaction.class));
    }

    @Test
    void earnPoints_silverTier_applies125xMultiplier() {
        testAccount.setTier(Tier.SILVER);
        testAccount.setPoints(500);
        testAccount.setTotalPointsEarned(500);

        Order order = Order.builder()
                .id(20L)
                .orderNumber("ORD-20260213-0002")
                .totalAmount(new BigDecimal("100.00"))
                .build();

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyAccountResponse response = loyaltyService.earnPoints(1L, order);

        // 100 * 10 = 1000 base, * 1.25 = 1250 earned
        assertEquals(1750, response.getPoints()); // 500 + 1250
        assertEquals(1750, response.getTotalPointsEarned());
    }

    @Test
    void earnPoints_goldTier_applies15xMultiplier() {
        testAccount.setTier(Tier.GOLD);
        testAccount.setPoints(2000);
        testAccount.setTotalPointsEarned(2000);

        Order order = Order.builder()
                .id(21L)
                .orderNumber("ORD-20260213-0003")
                .totalAmount(new BigDecimal("100.00"))
                .build();

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyAccountResponse response = loyaltyService.earnPoints(1L, order);

        // 100 * 10 = 1000 base, * 1.5 = 1500 earned
        assertEquals(3500, response.getPoints()); // 2000 + 1500
        assertEquals(3500, response.getTotalPointsEarned());
    }

    @Test
    void earnPoints_platinumTier_applies2xMultiplier() {
        testAccount.setTier(Tier.PLATINUM);
        testAccount.setPoints(5000);
        testAccount.setTotalPointsEarned(5000);

        Order order = Order.builder()
                .id(22L)
                .orderNumber("ORD-20260213-0004")
                .totalAmount(new BigDecimal("50.00"))
                .build();

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyAccountResponse response = loyaltyService.earnPoints(1L, order);

        // 50 * 10 = 500 base, * 2.0 = 1000 earned
        assertEquals(6000, response.getPoints()); // 5000 + 1000
        assertEquals(6000, response.getTotalPointsEarned());
    }

    @Test
    void earnPoints_noExistingAccount_createsAccountAndEarns() {
        LoyaltyAccount newAccount = LoyaltyAccount.builder()
                .id(11L)
                .customer(testCustomer)
                .points(0)
                .totalPointsEarned(0)
                .tier(Tier.BRONZE)
                .createdAt(LocalDateTime.now())
                .build();

        Order order = Order.builder()
                .id(20L)
                .orderNumber("ORD-20260213-0005")
                .totalAmount(new BigDecimal("50.00"))
                .build();

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> {
            LoyaltyAccount a = inv.getArgument(0);
            if (a.getId() == null) {
                a.setId(11L);
            }
            return a;
        });
        when(transactionRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyAccountResponse response = loyaltyService.earnPoints(1L, order);

        // 50 * 10 = 500 points, 1.0x multiplier
        assertEquals(500, response.getPoints());
        assertEquals(500, response.getTotalPointsEarned());
    }

    // ---- redeemPoints tests ----

    @Test
    void redeemPoints_success_deductsPoints() {
        testAccount.setPoints(500);

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyAccountResponse response = loyaltyService.redeemPoints(1L, 200);

        assertEquals(300, response.getPoints()); // 500 - 200
        verify(transactionRepository).save(any(LoyaltyTransaction.class));
    }

    @Test
    void redeemPoints_insufficientPoints_throwsException() {
        testAccount.setPoints(50);

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> loyaltyService.redeemPoints(1L, 200));

        assertTrue(exception.getMessage().contains("Insufficient points"));
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void redeemPoints_zeroPoints_throwsException() {
        testAccount.setPoints(500);

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> loyaltyService.redeemPoints(1L, 0));

        assertEquals("Points must be positive", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void redeemPoints_negativePoints_throwsException() {
        testAccount.setPoints(500);

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> loyaltyService.redeemPoints(1L, -10));

        assertEquals("Points must be positive", exception.getMessage());
    }

    @Test
    void redeemPoints_accountNotFound_throwsException() {
        when(accountRepository.findByCustomerId(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> loyaltyService.redeemPoints(999L, 100));
    }

    @Test
    void redeemPoints_exactBalance_succeeds() {
        testAccount.setPoints(200);

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyAccountResponse response = loyaltyService.redeemPoints(1L, 200);

        assertEquals(0, response.getPoints());
    }

    // ---- tier upgrade tests ----

    @Test
    void earnPoints_upgradesFromBronzeToSilver() {
        testAccount.setPoints(0);
        testAccount.setTotalPointsEarned(0);
        testAccount.setTier(Tier.BRONZE);

        Order order = Order.builder()
                .id(30L)
                .orderNumber("ORD-20260213-0010")
                .totalAmount(new BigDecimal("50.00"))
                .build();

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyAccountResponse response = loyaltyService.earnPoints(1L, order);

        // 50 * 10 = 500 points -> exactly SILVER_THRESHOLD
        assertEquals("SILVER", response.getTier());
        assertEquals(500, response.getTotalPointsEarned());
    }

    @Test
    void earnPoints_upgradesFromSilverToGold() {
        testAccount.setPoints(500);
        testAccount.setTotalPointsEarned(500);
        testAccount.setTier(Tier.SILVER);

        // Need to earn enough to reach 2000 total
        // With SILVER 1.25x: need base 1200 -> 1200 * 1.25 = 1500 -> total = 500 + 1500 = 2000
        Order order = Order.builder()
                .id(31L)
                .orderNumber("ORD-20260213-0011")
                .totalAmount(new BigDecimal("120.00"))
                .build();

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyAccountResponse response = loyaltyService.earnPoints(1L, order);

        // 120 * 10 = 1200 base, * 1.25 = 1500 earned; total = 500 + 1500 = 2000
        assertEquals("GOLD", response.getTier());
        assertEquals(2000, response.getTotalPointsEarned());
    }

    @Test
    void earnPoints_upgradesFromGoldToPlatinum() {
        testAccount.setPoints(2000);
        testAccount.setTotalPointsEarned(2000);
        testAccount.setTier(Tier.GOLD);

        // Need to reach 5000 total. With GOLD 1.5x: need base 2000 -> 2000 * 1.5 = 3000 -> total = 5000
        Order order = Order.builder()
                .id(32L)
                .orderNumber("ORD-20260213-0012")
                .totalAmount(new BigDecimal("200.00"))
                .build();

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyAccountResponse response = loyaltyService.earnPoints(1L, order);

        // 200 * 10 = 2000 base, * 1.5 = 3000 earned; total = 2000 + 3000 = 5000
        assertEquals("PLATINUM", response.getTier());
        assertEquals(5000, response.getTotalPointsEarned());
    }

    @Test
    void earnPoints_belowThreshold_remainsBronze() {
        testAccount.setPoints(0);
        testAccount.setTotalPointsEarned(0);
        testAccount.setTier(Tier.BRONZE);

        Order order = Order.builder()
                .id(33L)
                .orderNumber("ORD-20260213-0013")
                .totalAmount(new BigDecimal("10.00"))
                .build();

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(LoyaltyTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        LoyaltyAccountResponse response = loyaltyService.earnPoints(1L, order);

        // 10 * 10 = 100 points, still below 500
        assertEquals("BRONZE", response.getTier());
        assertEquals(100, response.getTotalPointsEarned());
    }

    @Test
    void getOrCreateAccount_pointsToNextTier_correctForBronze() {
        testAccount.setPoints(100);
        testAccount.setTotalPointsEarned(100);
        testAccount.setTier(Tier.BRONZE);

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));

        LoyaltyAccountResponse response = loyaltyService.getOrCreateAccount(1L);

        // BRONZE -> SILVER threshold is 500, 500 - 100 = 400
        assertEquals(400, response.getPointsToNextTier());
    }

    @Test
    void getOrCreateAccount_pointsToNextTier_zeroForPlatinum() {
        testAccount.setTier(Tier.PLATINUM);
        testAccount.setTotalPointsEarned(6000);

        when(accountRepository.findByCustomerId(1L)).thenReturn(Optional.of(testAccount));

        LoyaltyAccountResponse response = loyaltyService.getOrCreateAccount(1L);

        assertEquals(0, response.getPointsToNextTier());
    }
}
