package com.sport_pro_be.modules.membership.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.membership.constant.MembershipMessageConstant;
import com.sport_pro_be.modules.membership.dto.TierConfigRequest;
import com.sport_pro_be.modules.membership.dto.TierConfigResponse;
import com.sport_pro_be.modules.membership.interfaces.IAdminTierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tier-configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TierConfigController {

    private final IAdminTierService adminTierService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TierConfigResponse>>> getAllConfigs() {
        List<TierConfigResponse> response = adminTierService.getAllTierConfigs();
        return ResponseEntity.ok(ApiResponse.of(MembershipMessageConstant.TIER_CONFIGS_RETRIEVED, response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TierConfigResponse>> updateConfig(
            @PathVariable Long id,
            @RequestBody TierConfigRequest request) {
        TierConfigResponse response = adminTierService.updateTierConfig(id, request);
        return ResponseEntity.ok(ApiResponse.of(MembershipMessageConstant.TIER_CONFIG_UPDATED, response));
    }
}
