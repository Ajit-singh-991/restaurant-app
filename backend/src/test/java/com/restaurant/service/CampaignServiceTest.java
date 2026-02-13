package com.restaurant.service;

import com.restaurant.dto.CampaignDto.*;
import com.restaurant.entity.Campaign;
import com.restaurant.entity.Campaign.CampaignStatus;
import com.restaurant.entity.Campaign.CampaignType;
import com.restaurant.entity.User;
import com.restaurant.repository.CampaignRepository;
import com.restaurant.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CampaignService campaignService;

    private User testAdmin;
    private Campaign testCampaign;

    @BeforeEach
    void setUp() {
        testAdmin = User.builder()
                .id(1L)
                .username("admin")
                .fullName("Admin User")
                .role(User.Role.ADMIN)
                .build();

        testCampaign = Campaign.builder()
                .id(10L)
                .name("Summer Sale")
                .description("20% off all items")
                .type(CampaignType.PERCENTAGE_DISCOUNT)
                .status(CampaignStatus.ACTIVE)
                .discountPercent(new BigDecimal("20.00"))
                .promoCode("SUMMER20")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .targetSegment("ALL")
                .usageCount(5)
                .maxUsage(100)
                .createdBy(testAdmin)
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();
    }

    // ---- createCampaign tests ----

    @Test
    void createCampaign_success_returnsCampaignResponse() {
        CreateCampaignRequest request = CreateCampaignRequest.builder()
                .name("Summer Sale")
                .description("20% off all items")
                .type("PERCENTAGE_DISCOUNT")
                .discountPercent(new BigDecimal("20.00"))
                .promoCode("SUMMER20")
                .startDate(LocalDateTime.now().minusDays(1).toString())
                .endDate(LocalDateTime.now().plusDays(30).toString())
                .targetSegment("ALL")
                .maxUsage(100)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(campaignRepository.findByPromoCode("SUMMER20")).thenReturn(Optional.empty());
        when(campaignRepository.save(any(Campaign.class))).thenReturn(testCampaign);

        CampaignResponse response = campaignService.createCampaign(request, 1L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Summer Sale", response.getName());
        assertEquals("20% off all items", response.getDescription());
        assertEquals("PERCENTAGE_DISCOUNT", response.getType());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals(new BigDecimal("20.00"), response.getDiscountPercent());
        assertEquals("SUMMER20", response.getPromoCode());
        assertEquals("ALL", response.getTargetSegment());
        assertEquals(5, response.getUsageCount());
        assertEquals(100, response.getMaxUsage());
        assertEquals("Admin User", response.getCreatedBy());
        verify(campaignRepository).save(any(Campaign.class));
    }

    @Test
    void createCampaign_duplicatePromoCode_throwsException() {
        CreateCampaignRequest request = CreateCampaignRequest.builder()
                .name("Another Sale")
                .type("PERCENTAGE_DISCOUNT")
                .promoCode("SUMMER20")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testAdmin));
        when(campaignRepository.findByPromoCode("SUMMER20")).thenReturn(Optional.of(testCampaign));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignService.createCampaign(request, 1L));

        assertEquals("Promo code already exists", exception.getMessage());
        verify(campaignRepository, never()).save(any());
    }

    @Test
    void createCampaign_userNotFound_throwsException() {
        CreateCampaignRequest request = CreateCampaignRequest.builder()
                .name("Test Campaign")
                .type("PERCENTAGE_DISCOUNT")
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> campaignService.createCampaign(request, 999L));

        verify(campaignRepository, never()).save(any());
    }

    // ---- getAllCampaigns tests ----

    @Test
    void getAllCampaigns_returnsList() {
        Campaign campaign2 = Campaign.builder()
                .id(11L)
                .name("Winter Deal")
                .description("Free delivery")
                .type(CampaignType.FREE_DELIVERY)
                .status(CampaignStatus.DRAFT)
                .promoCode("WINTER")
                .usageCount(0)
                .createdBy(testAdmin)
                .createdAt(LocalDateTime.now())
                .build();

        when(campaignRepository.findAll()).thenReturn(List.of(testCampaign, campaign2));

        List<CampaignResponse> result = campaignService.getAllCampaigns();

        assertEquals(2, result.size());
        assertEquals("Summer Sale", result.get(0).getName());
        assertEquals("Winter Deal", result.get(1).getName());
    }

    @Test
    void getAllCampaigns_noCampaigns_returnsEmptyList() {
        when(campaignRepository.findAll()).thenReturn(List.of());

        List<CampaignResponse> result = campaignService.getAllCampaigns();

        assertTrue(result.isEmpty());
    }

    // ---- getActiveCampaigns tests ----

    @Test
    void getActiveCampaigns_returnsOnlyActive() {
        when(campaignRepository.findActiveCampaigns(any(LocalDateTime.class)))
                .thenReturn(List.of(testCampaign));

        List<CampaignResponse> result = campaignService.getActiveCampaigns();

        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).getStatus());
        assertEquals("Summer Sale", result.get(0).getName());
    }

    // ---- updateStatus tests ----

    @Test
    void updateStatus_success_returnsUpdatedCampaign() {
        Campaign pausedCampaign = Campaign.builder()
                .id(10L)
                .name("Summer Sale")
                .description("20% off all items")
                .type(CampaignType.PERCENTAGE_DISCOUNT)
                .status(CampaignStatus.PAUSED)
                .discountPercent(new BigDecimal("20.00"))
                .promoCode("SUMMER20")
                .usageCount(5)
                .maxUsage(100)
                .createdBy(testAdmin)
                .createdAt(LocalDateTime.now())
                .build();

        when(campaignRepository.findById(10L)).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenReturn(pausedCampaign);

        CampaignResponse response = campaignService.updateStatus(10L, "PAUSED");

        assertEquals("PAUSED", response.getStatus());
        verify(campaignRepository).save(any(Campaign.class));
    }

    @Test
    void updateStatus_campaignNotFound_throwsException() {
        when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campaignService.updateStatus(999L, "PAUSED"));

        assertEquals("Campaign not found", exception.getMessage());
        verify(campaignRepository, never()).save(any());
    }

    // ---- validatePromoCode tests ----

    @Test
    void validatePromoCode_valid_returnsValidResponse() {
        when(campaignRepository.findByPromoCode("SUMMER20")).thenReturn(Optional.of(testCampaign));

        ValidatePromoResponse response = campaignService.validatePromoCode("SUMMER20");

        assertTrue(response.isValid());
        assertEquals("Promo code is valid", response.getMessage());
        assertEquals(new BigDecimal("20.00"), response.getDiscountPercent());
        assertEquals("PERCENTAGE_DISCOUNT", response.getType());
    }

    @Test
    void validatePromoCode_inactive_returnsInvalidResponse() {
        testCampaign.setStatus(CampaignStatus.PAUSED);

        when(campaignRepository.findByPromoCode("SUMMER20")).thenReturn(Optional.of(testCampaign));

        ValidatePromoResponse response = campaignService.validatePromoCode("SUMMER20");

        assertFalse(response.isValid());
        assertEquals("Campaign is not active", response.getMessage());
    }

    @Test
    void validatePromoCode_expired_returnsInvalidResponse() {
        testCampaign.setEndDate(LocalDateTime.now().minusDays(1));

        when(campaignRepository.findByPromoCode("SUMMER20")).thenReturn(Optional.of(testCampaign));

        ValidatePromoResponse response = campaignService.validatePromoCode("SUMMER20");

        assertFalse(response.isValid());
        assertEquals("Promo code has expired", response.getMessage());
    }

    @Test
    void validatePromoCode_usageLimitReached_returnsInvalidResponse() {
        testCampaign.setUsageCount(100);
        testCampaign.setMaxUsage(100);

        when(campaignRepository.findByPromoCode("SUMMER20")).thenReturn(Optional.of(testCampaign));

        ValidatePromoResponse response = campaignService.validatePromoCode("SUMMER20");

        assertFalse(response.isValid());
        assertEquals("Promo code usage limit reached", response.getMessage());
    }

    @Test
    void validatePromoCode_notFound_returnsInvalidResponse() {
        when(campaignRepository.findByPromoCode("INVALID")).thenReturn(Optional.empty());

        ValidatePromoResponse response = campaignService.validatePromoCode("INVALID");

        assertFalse(response.isValid());
        assertEquals("Invalid promo code", response.getMessage());
    }

    @Test
    void validatePromoCode_noMaxUsage_returnsValidResponse() {
        testCampaign.setMaxUsage(null);
        testCampaign.setUsageCount(9999);

        when(campaignRepository.findByPromoCode("SUMMER20")).thenReturn(Optional.of(testCampaign));

        ValidatePromoResponse response = campaignService.validatePromoCode("SUMMER20");

        assertTrue(response.isValid());
        assertEquals("Promo code is valid", response.getMessage());
    }

    @Test
    void validatePromoCode_noEndDate_returnsValidResponse() {
        testCampaign.setEndDate(null);

        when(campaignRepository.findByPromoCode("SUMMER20")).thenReturn(Optional.of(testCampaign));

        ValidatePromoResponse response = campaignService.validatePromoCode("SUMMER20");

        assertTrue(response.isValid());
        assertEquals("Promo code is valid", response.getMessage());
    }

    // ---- redeemPromoCode tests ----

    @Test
    void redeemPromoCode_valid_incrementsUsageCount() {
        when(campaignRepository.findByPromoCode("SUMMER20")).thenReturn(Optional.of(testCampaign));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));

        ValidatePromoResponse response = campaignService.redeemPromoCode("SUMMER20");

        assertTrue(response.isValid());
        assertEquals(6, testCampaign.getUsageCount());
        verify(campaignRepository).save(testCampaign);
    }

    @Test
    void redeemPromoCode_invalid_doesNotIncrementUsage() {
        when(campaignRepository.findByPromoCode("INVALID")).thenReturn(Optional.empty());

        ValidatePromoResponse response = campaignService.redeemPromoCode("INVALID");

        assertFalse(response.isValid());
        verify(campaignRepository, never()).save(any());
    }

    // ---- getCampaignStats tests ----

    @Test
    void getCampaignStats_withCampaigns_returnsCorrectStats() {
        Campaign campaign2 = Campaign.builder()
                .id(11L)
                .name("Winter Deal")
                .type(CampaignType.FREE_DELIVERY)
                .status(CampaignStatus.ACTIVE)
                .usageCount(10)
                .createdBy(testAdmin)
                .createdAt(LocalDateTime.now())
                .build();

        when(campaignRepository.findAll()).thenReturn(List.of(testCampaign, campaign2));
        when(campaignRepository.findActiveCampaigns(any(LocalDateTime.class)))
                .thenReturn(List.of(testCampaign, campaign2));

        CampaignStats stats = campaignService.getCampaignStats();

        assertEquals(2, stats.getTotalCampaigns());
        assertEquals(2, stats.getActiveCampaigns());
        assertEquals(15, stats.getTotalRedemptions()); // 5 + 10
        assertNotNull(stats.getTopCampaigns());
        assertEquals(2, stats.getTopCampaigns().size());
        // Top campaign should be sorted by usageCount desc
        assertEquals("Winter Deal", stats.getTopCampaigns().get(0).getName());
        assertEquals("Summer Sale", stats.getTopCampaigns().get(1).getName());
    }

    @Test
    void getCampaignStats_noCampaigns_returnsZeroStats() {
        when(campaignRepository.findAll()).thenReturn(List.of());
        when(campaignRepository.findActiveCampaigns(any(LocalDateTime.class)))
                .thenReturn(List.of());

        CampaignStats stats = campaignService.getCampaignStats();

        assertEquals(0, stats.getTotalCampaigns());
        assertEquals(0, stats.getActiveCampaigns());
        assertEquals(0, stats.getTotalRedemptions());
        assertTrue(stats.getTopCampaigns().isEmpty());
    }

    @Test
    void getCampaignStats_topCampaignsLimitedToFive() {
        List<Campaign> manyCampaigns = List.of(
                buildCampaignWithUsage(1L, "C1", 50),
                buildCampaignWithUsage(2L, "C2", 40),
                buildCampaignWithUsage(3L, "C3", 30),
                buildCampaignWithUsage(4L, "C4", 20),
                buildCampaignWithUsage(5L, "C5", 10),
                buildCampaignWithUsage(6L, "C6", 5),
                buildCampaignWithUsage(7L, "C7", 1)
        );

        when(campaignRepository.findAll()).thenReturn(manyCampaigns);
        when(campaignRepository.findActiveCampaigns(any(LocalDateTime.class)))
                .thenReturn(List.of());

        CampaignStats stats = campaignService.getCampaignStats();

        assertEquals(7, stats.getTotalCampaigns());
        assertEquals(5, stats.getTopCampaigns().size());
        assertEquals("C1", stats.getTopCampaigns().get(0).getName());
        assertEquals("C5", stats.getTopCampaigns().get(4).getName());
    }

    private Campaign buildCampaignWithUsage(Long id, String name, int usageCount) {
        return Campaign.builder()
                .id(id)
                .name(name)
                .type(CampaignType.PERCENTAGE_DISCOUNT)
                .status(CampaignStatus.ACTIVE)
                .usageCount(usageCount)
                .createdBy(testAdmin)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
