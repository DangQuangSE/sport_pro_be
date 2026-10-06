package com.sport_pro_be.modules.public_config.service;

import com.sport_pro_be.common.SecurityUtils;
import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.exception.ConflictException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.public_config.constant.PublicConfigMessageConstant;
import com.sport_pro_be.modules.public_config.domain.ConfigType;
import com.sport_pro_be.modules.public_config.domain.PublicConfig;
import com.sport_pro_be.modules.public_config.domain.SettingChangeAudit;
import com.sport_pro_be.modules.public_config.domain.SettingScope;
import com.sport_pro_be.modules.public_config.dto.request.PublicConfigRequest;
import com.sport_pro_be.modules.public_config.dto.request.PublicConfigUpdateRequest;
import com.sport_pro_be.modules.public_config.dto.response.PublicConfigResponse;
import com.sport_pro_be.modules.public_config.interfaces.IPublicConfigService;
import com.sport_pro_be.modules.public_config.repository.PublicConfigRepository;
import com.sport_pro_be.modules.public_config.repository.SettingChangeAuditRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicConfigService implements IPublicConfigService {

    private static final Set<String> REQUIRED_PRICING_KEYS = Set.of(
            "checkout.shipping.standard_fee",
            "checkout.shipping.free_threshold",
            "checkout.shipping.active",
            "checkout.tax.rate",
            "checkout.tax.taxable_base",
            "checkout.money.rounding_mode",
            "checkout.discount.stacking_policy",
            "checkout.shipping.threshold_base"
    );

    private final PublicConfigRepository publicConfigRepository;
    private final SettingChangeAuditRepository settingChangeAuditRepository;
    private final SettingDefinitionRegistry settingDefinitionRegistry;

    @Override
    @Transactional(readOnly = true)
    public List<PublicConfigResponse> getAllConfigs() {
        return publicConfigRepository
                .findAllByScopeAndActiveTrueOrderByCategoryAscConfigKeyAsc(SettingScope.PUBLIC)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicConfigResponse> getAdminConfigs() {
        return publicConfigRepository.findAllByOrderByCategoryAscConfigKeyAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getConfigsAsMap() {
        return publicConfigRepository
                .findAllByScopeAndActiveTrueOrderByCategoryAscConfigKeyAsc(SettingScope.PUBLIC)
                .stream()
                .collect(Collectors.toMap(
                        PublicConfig::getConfigKey,
                        config -> config.getConfigValue() != null ? config.getConfigValue() : ""));
    }

    @Override
    @Transactional(readOnly = true)
    public PublicConfigResponse getConfigByKey(String key) {
        PublicConfig config = publicConfigRepository
                .findByConfigKeyAndScopeAndActiveTrue(normalizeKey(key), SettingScope.PUBLIC)
                .orElseThrow(() -> new ResourceNotFoundException(
                        PublicConfigMessageConstant.CONFIG_NOT_FOUND + ": " + key));
        return mapToResponse(config);
    }

    @Override
    @Transactional
    public PublicConfigResponse createConfig(PublicConfigRequest request) {
        String key = normalizeKey(request.getConfigKey());
        if (publicConfigRepository.existsByConfigKey(key)) {
            throw new ConflictException(PublicConfigMessageConstant.CONFIG_ALREADY_EXISTS + ": " + key);
        }

        SettingDefinition definition = settingDefinitionRegistry.find(key).orElse(null);
        SettingScope scope = request.getScope() != null ? request.getScope() : SettingScope.INTERNAL;
        boolean active = request.getActive() != null && request.getActive();
        String category = definition != null ? definition.category() : defaultValue(request.getCategory(), "CONTENT");
        String unit = definition != null ? definition.unit() : trimToNull(request.getUnit());
        String validationRules = definition != null ? definition.validationRules() : request.getValidationRules();
        validateKeyAndValue(key, request.getConfigValue(), request.getConfigType(), category, unit,
                validationRules, scope);

        PublicConfig config = PublicConfig.builder()
                .configKey(key)
                .configValue(request.getConfigValue())
                .configType(request.getConfigType())
                .description(request.getDescription())
                .category(category)
                .unit(unit)
                .scope(scope)
                .active(active)
                .validationRules(validationRules)
                .updatedBy(SecurityUtils.getCurrentUserId())
                .build();

        try {
            config = publicConfigRepository.saveAndFlush(config);
        } catch (OptimisticLockingFailureException | OptimisticLockException exception) {
            throw new ConflictException(PublicConfigMessageConstant.CONFIG_VERSION_CONFLICT);
        }
        saveAudit(config, null, snapshot(config));
        return mapToResponse(config);
    }

    @Override
    @Transactional
    public PublicConfigResponse updateConfig(String key, PublicConfigUpdateRequest request) {
        // Compatibility route: aliases use the same optimistic-lock contract.
        return updateSetting(key, request);
    }

    @Override
    @Transactional
    public PublicConfigResponse updateSetting(String key, PublicConfigUpdateRequest request) {
        if (request.getExpectedVersion() == null) {
            throw new BadRequestException("expectedVersion is required");
        }
        return updateInternal(key, request);
    }

    @Override
    @Transactional
    public void setActive(String key, boolean active, Long expectedVersion) {
        if (expectedVersion == null) {
            throw new BadRequestException("expectedVersion is required");
        }
        PublicConfigUpdateRequest request = PublicConfigUpdateRequest.builder()
                .active(active)
                .expectedVersion(expectedVersion)
                .build();
        updateInternal(key, request);
    }

    @Override
    @Transactional
    public void deleteConfig(String key) {
        throw new BadRequestException("expectedVersion is required");
    }

    @Override
    @Transactional
    public void deleteConfig(String key, Long expectedVersion) {
        if (expectedVersion == null) {
            throw new BadRequestException("expectedVersion is required");
        }
        PublicConfig config = findConfig(key);
        assertExpectedVersion(config, expectedVersion);
        if (REQUIRED_PRICING_KEYS.contains(normalizeKey(key)) && config.isActive()) {
            throw new ConflictException(PublicConfigMessageConstant.CONFIG_REQUIRED);
        }

        saveAudit(config, snapshot(config), null);
        try {
            publicConfigRepository.deleteAndFlush(config);
        } catch (OptimisticLockingFailureException | OptimisticLockException exception) {
            throw new ConflictException(PublicConfigMessageConstant.CONFIG_VERSION_CONFLICT);
        }
    }

    private PublicConfigResponse updateInternal(String key, PublicConfigUpdateRequest request) {
        PublicConfig config = findConfig(key);
        assertExpectedVersion(config, request.getExpectedVersion());

        String before = snapshot(config);
        if (request.getConfigValue() != null) {
            config.setConfigValue(request.getConfigValue());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            config.setCategory(request.getCategory());
        }
        if (request.getUnit() != null) {
            config.setUnit(request.getUnit());
        }
        if (request.getScope() != null) {
            config.setScope(request.getScope());
        }
        if (request.getActive() != null) {
            config.setActive(request.getActive());
        }
        if (request.getValidationRules() != null) {
            config.setValidationRules(request.getValidationRules());
        }
        validateKeyAndValue(config.getConfigKey(), config.getConfigValue(), config.getConfigType(),
                config.getCategory(), config.getUnit(), config.getValidationRules(), config.getScope());
        config.setUpdatedBy(SecurityUtils.getCurrentUserId());

        try {
            config = publicConfigRepository.saveAndFlush(config);
        } catch (OptimisticLockingFailureException | OptimisticLockException exception) {
            throw new ConflictException(PublicConfigMessageConstant.CONFIG_VERSION_CONFLICT);
        }
        saveAudit(config, before, snapshot(config));
        return mapToResponse(config);
    }

    private PublicConfig findConfig(String key) {
        return publicConfigRepository.findByConfigKey(normalizeKey(key))
                .orElseThrow(() -> new ResourceNotFoundException(
                        PublicConfigMessageConstant.CONFIG_NOT_FOUND + ": " + key));
    }

    private void assertExpectedVersion(PublicConfig config, Long expectedVersion) {
        if (expectedVersion == null || !expectedVersion.equals(config.getVersion())) {
            throw new ConflictException(PublicConfigMessageConstant.CONFIG_VERSION_CONFLICT);
        }
    }

    private void validateKeyAndValue(
            String key,
            String value,
            ConfigType type,
            String category,
            String unit,
            String validationRules,
            SettingScope scope) {
        String normalizedKey = normalizeKey(key);
        if (containsInfrastructureSecretName(normalizedKey)) {
            throw new BadRequestException(PublicConfigMessageConstant.CONFIG_SECRET_NOT_ALLOWED);
        }

        SettingDefinition definition = settingDefinitionRegistry.find(normalizedKey).orElse(null);
        if (definition != null) {
            if (scope != SettingScope.INTERNAL
                    || !definition.category().equalsIgnoreCase(defaultValue(category, ""))
                    || !Objects.equals(definition.unit(), trimToNull(unit))
                    || !Objects.equals(definition.validationRules(), validationRules)) {
                throw new BadRequestException(PublicConfigMessageConstant.CONFIG_METADATA_MISMATCH);
            }
            if (type != definition.configType()) {
                throw new BadRequestException(PublicConfigMessageConstant.CONFIG_INVALID_VALUE);
            }
            if (definition.required() && (value == null || value.isBlank())) {
                throw new BadRequestException(PublicConfigMessageConstant.CONFIG_INVALID_VALUE);
            }
            validateTypedValue(value, type, definition);
            return;
        }

        if (scope == SettingScope.PUBLIC && !settingDefinitionRegistry.isPublicContentKey(normalizedKey)) {
            throw new BadRequestException(PublicConfigMessageConstant.CONFIG_PUBLIC_KEY_NOT_ALLOWED);
        }
        validateTypedValue(value, type, null);
    }

    private void validateTypedValue(String value, ConfigType type, SettingDefinition definition) {
        if (value == null || type == null) {
            if (definition != null && definition.required()) {
                throw new BadRequestException(PublicConfigMessageConstant.CONFIG_INVALID_VALUE);
            }
            return;
        }

        try {
            if (type == ConfigType.NUMBER) {
                BigDecimal number = new BigDecimal(value);
                if (definition != null) {
                    if (definition.minimum() != null && number.compareTo(definition.minimum()) < 0) {
                        throw new BadRequestException(PublicConfigMessageConstant.CONFIG_INVALID_VALUE);
                    }
                    if (definition.maximum() != null && number.compareTo(definition.maximum()) > 0) {
                        throw new BadRequestException(PublicConfigMessageConstant.CONFIG_INVALID_VALUE);
                    }
                    if (definition.wholeVnd() && number.stripTrailingZeros().scale() > 0) {
                        throw new BadRequestException(PublicConfigMessageConstant.CONFIG_INVALID_VALUE);
                    }
                }
            } else if (type == ConfigType.BOOLEAN
                    && !value.equalsIgnoreCase("true")
                    && !value.equalsIgnoreCase("false")) {
                throw new BadRequestException(PublicConfigMessageConstant.CONFIG_INVALID_VALUE);
            }
            if (definition != null && definition.hasAllowedValues() && !definition.allowedValues().contains(value)) {
                throw new BadRequestException(PublicConfigMessageConstant.CONFIG_INVALID_VALUE);
            }
        } catch (NumberFormatException exception) {
            throw new BadRequestException(PublicConfigMessageConstant.CONFIG_INVALID_VALUE);
        }
    }

    private boolean containsInfrastructureSecretName(String key) {
        return key.contains("password") || key.contains("secret")
                || key.contains("token") || key.contains("credential")
                || key.contains("api_key") || key.contains("private_key");
    }

    private void saveAudit(PublicConfig config, String before, String after) {
        settingChangeAuditRepository.save(SettingChangeAudit.builder()
                .settingKey(config.getConfigKey())
                .actorId(SecurityUtils.getCurrentUserId())
                .version(config.getVersion() == null ? 0L : config.getVersion())
                .beforeValue(redact(before))
                .afterValue(redact(after))
                .build());
    }

    private String snapshot(PublicConfig config) {
        return "valueHash=" + hash(config.getConfigValue())
                + ";active=" + config.isActive()
                + ";scope=" + config.getScope()
                + ";type=" + config.getConfigType()
                + ";version=" + (config.getVersion() == null ? 0L : config.getVersion());
    }

    private String redact(String value) {
        if (value == null || value.startsWith("valueHash=")) {
            return value;
        }
        return "[REDACTED]";
    }

    private String hash(String value) {
        if (value == null) {
            return "null";
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                result.append(String.format(Locale.ROOT, "%02x", item));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String normalizeKey(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
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
                .category(config.getCategory())
                .unit(config.getUnit())
                .scope(config.getScope())
                .active(config.isActive())
                .validationRules(config.getValidationRules())
                .version(config.getVersion())
                .updatedBy(config.getUpdatedBy())
                .build();
    }
}
