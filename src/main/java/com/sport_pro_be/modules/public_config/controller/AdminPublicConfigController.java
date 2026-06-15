package com.sport_pro_be.modules.public_config.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.public_config.constant.PublicConfigMessageConstant;
import com.sport_pro_be.modules.public_config.dto.request.PublicConfigRequest;
import com.sport_pro_be.modules.public_config.dto.request.PublicConfigUpdateRequest;
import com.sport_pro_be.modules.public_config.dto.response.PublicConfigResponse;
import com.sport_pro_be.modules.public_config.interfaces.IPublicConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/public-configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPublicConfigController {

    private final IPublicConfigService publicConfigService;

    @PostMapping
    public ApiResponse<PublicConfigResponse> createConfig(@Valid @RequestBody PublicConfigRequest request) {
        return ApiResponse.of(PublicConfigMessageConstant.CONFIG_CREATED, publicConfigService.createConfig(request));
    }

    @PutMapping("/{key}")
    public ApiResponse<PublicConfigResponse> updateConfig(
            @PathVariable String key, 
            @Valid @RequestBody PublicConfigUpdateRequest request) {
        return ApiResponse.of(PublicConfigMessageConstant.CONFIG_UPDATED, publicConfigService.updateConfig(key, request));
    }

    @DeleteMapping("/{key}")
    public ApiResponse<Void> deleteConfig(@PathVariable String key) {
        publicConfigService.deleteConfig(key);
        return ApiResponse.of(PublicConfigMessageConstant.CONFIG_DELETED, null);
    }
}
