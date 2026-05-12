package com.sport_pro_be.modules.membership.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.membership.domain.TierConfig;
import com.sport_pro_be.modules.membership.dto.TierConfigRequest;
import com.sport_pro_be.modules.membership.dto.TierConfigResponse;
import com.sport_pro_be.modules.membership.repository.TierRepository;
import com.sport_pro_be.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/tier-configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TierConfigController {

    private final TierRepository tierRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TierConfigResponse>>> getAllConfigs() {
        List<TierConfigResponse> response = tierRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.of("Tier configurations retrieved", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TierConfigResponse>> updateConfig(
            @PathVariable Long id,
            @RequestBody TierConfigRequest request) {
        TierConfig config = tierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tier configuration not found"));
        
        config.setThreshold(request.getThreshold());
        config.setDescription(request.getDescription());
        
        config = tierRepository.save(config);
        return ResponseEntity.ok(ApiResponse.of("Tier configuration updated", mapToResponse(config)));
    }

    private TierConfigResponse mapToResponse(TierConfig config) {
        return TierConfigResponse.builder()
                .id(config.getId())
                .tier(config.getTier())
                .threshold(config.getThreshold())
                .description(config.getDescription())
                .build();
    }
}
