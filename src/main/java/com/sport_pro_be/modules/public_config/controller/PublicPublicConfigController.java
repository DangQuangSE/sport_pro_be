package com.sport_pro_be.modules.public_config.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.public_config.constant.PublicConfigMessageConstant;
import com.sport_pro_be.modules.public_config.dto.response.PublicConfigResponse;
import com.sport_pro_be.modules.public_config.interfaces.IPublicConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public-configs")
@RequiredArgsConstructor
public class PublicPublicConfigController {

    private final IPublicConfigService publicConfigService;

    @GetMapping
    public ApiResponse<List<PublicConfigResponse>> getAllConfigs() {
        return ApiResponse.of(PublicConfigMessageConstant.CONFIGS_RETRIEVED, publicConfigService.getAllConfigs());
    }

    @GetMapping("/map")
    public ApiResponse<Map<String, String>> getConfigsAsMap() {
        return ApiResponse.of(PublicConfigMessageConstant.CONFIGS_RETRIEVED, publicConfigService.getConfigsAsMap());
    }

    @GetMapping("/{key}")
    public ApiResponse<PublicConfigResponse> getConfigByKey(@PathVariable String key) {
        return ApiResponse.of(PublicConfigMessageConstant.CONFIGS_RETRIEVED, publicConfigService.getConfigByKey(key));
    }
}
