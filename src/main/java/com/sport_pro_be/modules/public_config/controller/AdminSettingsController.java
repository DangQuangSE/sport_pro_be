package com.sport_pro_be.modules.public_config.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.public_config.constant.PublicConfigMessageConstant;
import com.sport_pro_be.modules.public_config.dto.request.PublicConfigRequest;
import com.sport_pro_be.modules.public_config.dto.request.PublicConfigUpdateRequest;
import com.sport_pro_be.modules.public_config.dto.request.SettingActivationRequest;
import com.sport_pro_be.modules.public_config.dto.response.PublicConfigResponse;
import com.sport_pro_be.modules.public_config.interfaces.IPublicConfigService;
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
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    private final IPublicConfigService publicConfigService;

    @GetMapping
    public ApiResponse<List<PublicConfigResponse>> getSettings() {
        return ApiResponse.of(PublicConfigMessageConstant.CONFIGS_RETRIEVED,
                publicConfigService.getAdminConfigs());
    }

    @PostMapping
    public ApiResponse<PublicConfigResponse> createSetting(@Valid @RequestBody PublicConfigRequest request) {
        return ApiResponse.of(PublicConfigMessageConstant.CONFIG_CREATED,
                publicConfigService.createConfig(request));
    }

    @PutMapping("/{key}")
    public ApiResponse<PublicConfigResponse> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody PublicConfigUpdateRequest request) {
        return ApiResponse.of(PublicConfigMessageConstant.CONFIG_UPDATED,
                publicConfigService.updateSetting(key, request));
    }

    @PatchMapping("/{key}/active")
    public ApiResponse<Void> setActive(
            @PathVariable String key,
            @Valid @RequestBody SettingActivationRequest request) {
        publicConfigService.setActive(key, request.getActive(), request.getExpectedVersion());
        return ApiResponse.of(PublicConfigMessageConstant.CONFIG_UPDATED, null);
    }

    @DeleteMapping("/{key}")
    public ApiResponse<Void> deleteSetting(
            @PathVariable String key,
            @RequestParam Long expectedVersion) {
        publicConfigService.deleteConfig(key, expectedVersion);
        return ApiResponse.of(PublicConfigMessageConstant.CONFIG_DELETED, null);
    }
}
