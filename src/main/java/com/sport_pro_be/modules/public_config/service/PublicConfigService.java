package com.sport_pro_be.modules.public_config.service;

import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.exception.ConflictException;
import com.sport_pro_be.modules.public_config.domain.PublicConfig;
import com.sport_pro_be.modules.public_config.dto.request.PublicConfigRequest;
import com.sport_pro_be.modules.public_config.dto.request.PublicConfigUpdateRequest;
import com.sport_pro_be.modules.public_config.dto.response.PublicConfigResponse;
import com.sport_pro_be.modules.public_config.interfaces.IPublicConfigService;
import com.sport_pro_be.modules.public_config.repository.PublicConfigRepository;
import com.sport_pro_be.modules.public_config.constant.PublicConfigMessageConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicConfigService implements IPublicConfigService {

    private final PublicConfigRepository publicConfigRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PublicConfigResponse> getAllConfigs() {
        return publicConfigRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getConfigsAsMap() {
        return publicConfigRepository.findAll().stream()
                .collect(Collectors.toMap(
                        PublicConfig::getConfigKey,
                        c -> c.getConfigValue() != null ? c.getConfigValue() : ""));
    }

    @Override
    @Transactional(readOnly = true)
    public PublicConfigResponse getConfigByKey(String key) {
        PublicConfig config = publicConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException(PublicConfigMessageConstant.CONFIG_NOT_FOUND + ": " + key));
        return mapToResponse(config);
    }

    @Override
    @Transactional
    public PublicConfigResponse createConfig(PublicConfigRequest request) {
        if (publicConfigRepository.existsByConfigKey(request.getConfigKey())) {
            throw new ConflictException(PublicConfigMessageConstant.CONFIG_ALREADY_EXISTS + ": " + request.getConfigKey());
        }

        PublicConfig config = PublicConfig.builder()
                .configKey(request.getConfigKey())
                .configValue(request.getConfigValue())
                .configType(request.getConfigType())
                .description(request.getDescription())
                .build();

        config = publicConfigRepository.save(config);
        return mapToResponse(config);
    }

    @Override
    @Transactional
    public PublicConfigResponse updateConfig(String key, PublicConfigUpdateRequest request) {
        PublicConfig config = publicConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException(PublicConfigMessageConstant.CONFIG_NOT_FOUND + ": " + key));

        config.setConfigValue(request.getConfigValue());
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }

        config = publicConfigRepository.save(config);
        return mapToResponse(config);
    }

    @Override
    @Transactional
    public void deleteConfig(String key) {
        PublicConfig config = publicConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException(PublicConfigMessageConstant.CONFIG_NOT_FOUND + ": " + key));
        publicConfigRepository.delete(config);
    }

    private PublicConfigResponse mapToResponse(PublicConfig config) {
        return PublicConfigResponse.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .configType(config.getConfigType())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
