package com.sport_pro_be.modules.public_config.service;

import com.sport_pro_be.modules.public_config.domain.ConfigType;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Server-owned contract for a setting that affects application behaviour.
 * Requests may carry metadata for generic content settings, but commercial
 * settings are always validated against these definitions.
 */
public record SettingDefinition(
        String key,
        ConfigType configType,
        String category,
        String unit,
        BigDecimal minimum,
        BigDecimal maximum,
        boolean wholeVnd,
        Set<String> allowedValues,
        boolean required,
        String validationRules) {

    public boolean hasAllowedValues() {
        return allowedValues != null && !allowedValues.isEmpty();
    }
}
