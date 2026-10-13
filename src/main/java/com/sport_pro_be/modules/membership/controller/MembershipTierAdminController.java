package com.sport_pro_be.modules.membership.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.membership.constant.MembershipMessageConstant;
import com.sport_pro_be.modules.membership.dto.MembershipTierRequest;
import com.sport_pro_be.modules.membership.dto.MembershipTierResponse;
import com.sport_pro_be.modules.membership.interfaces.IAdminTierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/membership-tiers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MembershipTierAdminController {

    private final IAdminTierService adminTierService;

    @GetMapping
    public ApiResponse<List<MembershipTierResponse>> getTiers(
            @RequestParam(required = false) Boolean active) {
        return ApiResponse.of(MembershipMessageConstant.TIERS_RETRIEVED,
                adminTierService.getMembershipTiers(active));
    }

    @PostMapping
    public ApiResponse<MembershipTierResponse> createTier(
            @Valid @RequestBody MembershipTierRequest request) {
        return ApiResponse.of(MembershipMessageConstant.TIER_CREATED,
                adminTierService.createMembershipTier(request));
    }

    @PutMapping("/{code}")
    public ApiResponse<MembershipTierResponse> updateTier(
            @PathVariable String code,
            @Valid @RequestBody MembershipTierRequest request) {
        return ApiResponse.of(MembershipMessageConstant.TIER_UPDATED,
                adminTierService.updateMembershipTier(code, request));
    }

    @PatchMapping("/{code}/active")
    public ApiResponse<Void> setActive(
            @PathVariable String code,
            @RequestParam boolean active,
            @RequestParam Long expectedVersion) {
        adminTierService.setMembershipTierActive(code, active, expectedVersion);
        return ApiResponse.of(MembershipMessageConstant.TIER_UPDATED, null);
    }

    @DeleteMapping("/{code}")
    public ApiResponse<Void> deleteTier(
            @PathVariable String code,
            @RequestParam Long expectedVersion) {
        adminTierService.deleteMembershipTier(code, expectedVersion);
        return ApiResponse.of(MembershipMessageConstant.TIER_DELETED, null);
    }
}
