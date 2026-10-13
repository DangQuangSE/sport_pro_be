package com.sport_pro_be.modules.public_config.interfaces;

import com.sport_pro_be.modules.public_config.dto.request.PublicConfigRequest;
import com.sport_pro_be.modules.public_config.dto.request.PublicConfigUpdateRequest;
import com.sport_pro_be.modules.public_config.dto.response.PublicConfigResponse;

import java.util.List;
import java.util.Map;

public interface IPublicConfigService {
    List<PublicConfigResponse> getAllConfigs();
    List<PublicConfigResponse> getAdminConfigs();
    Map<String, String> getConfigsAsMap();
    PublicConfigResponse getConfigByKey(String key);
    PublicConfigResponse createConfig(PublicConfigRequest request);
    PublicConfigResponse updateConfig(String key, PublicConfigUpdateRequest request);
    PublicConfigResponse updateSetting(String key, PublicConfigUpdateRequest request);
    void setActive(String key, boolean active, Long expectedVersion);
    void deleteConfig(String key);
    void deleteConfig(String key, Long expectedVersion);
}
