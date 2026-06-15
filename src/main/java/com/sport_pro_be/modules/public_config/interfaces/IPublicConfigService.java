package com.sport_pro_be.modules.public_config.interfaces;

import com.sport_pro_be.modules.public_config.dto.request.PublicConfigRequest;
import com.sport_pro_be.modules.public_config.dto.request.PublicConfigUpdateRequest;
import com.sport_pro_be.modules.public_config.dto.response.PublicConfigResponse;

import java.util.List;
import java.util.Map;

public interface IPublicConfigService {
    List<PublicConfigResponse> getAllConfigs();
    Map<String, String> getConfigsAsMap();
    PublicConfigResponse getConfigByKey(String key);
    PublicConfigResponse createConfig(PublicConfigRequest request);
    PublicConfigResponse updateConfig(String key, PublicConfigUpdateRequest request);
    void deleteConfig(String key);
}
