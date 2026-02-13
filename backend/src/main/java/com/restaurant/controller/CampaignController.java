package com.restaurant.controller;

import com.restaurant.dto.CampaignDto.*;
import com.restaurant.security.UserPrincipal;
import com.restaurant.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CampaignResponse> create(
            @RequestBody CreateCampaignRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(campaignService.createCampaign(request, principal.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<CampaignResponse>> getAll() {
        return ResponseEntity.ok(campaignService.getAllCampaigns());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CampaignResponse>> getActive() {
        return ResponseEntity.ok(campaignService.getActiveCampaigns());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CampaignResponse> updateStatus(
            @PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(campaignService.updateStatus(id, status));
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<ValidatePromoResponse> validatePromo(@PathVariable String code) {
        return ResponseEntity.ok(campaignService.validatePromoCode(code));
    }

    @PostMapping("/redeem/{code}")
    public ResponseEntity<ValidatePromoResponse> redeemPromo(@PathVariable String code) {
        return ResponseEntity.ok(campaignService.redeemPromoCode(code));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CampaignStats> getStats() {
        return ResponseEntity.ok(campaignService.getCampaignStats());
    }
}
