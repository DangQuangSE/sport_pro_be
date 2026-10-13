package com.sport_pro_be.modules.membership.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.common.SecurityUtils;
import com.sport_pro_be.modules.membership.constant.MembershipMessageConstant;
import com.sport_pro_be.modules.membership.dto.MembershipProfileResponse;
import com.sport_pro_be.modules.membership.dto.MembershipTierResponse;
import com.sport_pro_be.modules.membership.interfaces.ITierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final ITierService tierService;

    @GetMapping("/tiers")
    public ApiResponse<List<MembershipTierResponse>> getActiveTiers() {
        List<MembershipTierResponse> tiers = tierService.getActiveTiers().stream()
                .map(tier -> MembershipTierResponse.builder()
                        .id(tier.getId())
                        .code(tier.getCode())
                        .displayNames(tier.getDisplayNames())
                        .descriptions(tier.getDescriptions())
                        .threshold(tier.getThreshold())
                        .sortOrder(tier.getSortOrder())
                        .discountPercentage(tier.getDiscountPercentage())
                        .freeShipping(tier.isFreeShipping())
                        .benefits(tier.getBenefits())
                        .active(tier.isActive())
                        .version(tier.getVersion())
                        .build())
                .toList();
        return ApiResponse.of(MembershipMessageConstant.TIERS_RETRIEVED, tiers);
    }

    @GetMapping("/me")
    public ApiResponse<MembershipProfileResponse> getMyMembership() {
        var user = SecurityUtils.getCurrentUser();
        return ApiResponse.of(MembershipMessageConstant.TIERS_RETRIEVED,
                tierService.getMembershipProfile(user));
    }
}
