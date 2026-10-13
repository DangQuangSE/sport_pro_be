package com.sport_pro_be.modules.pricing.service;

import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.modules.public_config.domain.PublicConfig;
import com.sport_pro_be.modules.public_config.domain.SettingScope;
import com.sport_pro_be.modules.public_config.repository.PublicConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Reads server-owned pricing settings without supplying unsafe defaults. */
@Component
@RequiredArgsConstructor
public class PricingSettingsReader {

    private static final String SHIPPING_FEE = "checkout.shipping.standard_fee";
    private static final String FREE_THRESHOLD = "checkout.shipping.free_threshold";
    private static final String SHIPPING_ACTIVE = "checkout.shipping.active";
    private static final String TAX_RATE = "checkout.tax.rate";
    private static final String TAXABLE_BASE = "checkout.tax.taxable_base";
    private static final String ROUNDING_MODE = "checkout.money.rounding_mode";
    private static final String STACKING_POLICY = "checkout.discount.stacking_policy";
    private static final String THRESHOLD_BASE = "checkout.shipping.threshold_base";

    private final PublicConfigRepository publicConfigRepository;

    public PricingSettings read() {
        Map<String, PublicConfig> configs = new LinkedHashMap<>();
        configs.put(SHIPPING_FEE, required(SHIPPING_FEE));
        configs.put(FREE_THRESHOLD, required(FREE_THRESHOLD));
        configs.put(SHIPPING_ACTIVE, required(SHIPPING_ACTIVE));
        configs.put(TAX_RATE, required(TAX_RATE));
        configs.put(TAXABLE_BASE, required(TAXABLE_BASE));
        configs.put(ROUNDING_MODE, required(ROUNDING_MODE));
        configs.put(STACKING_POLICY, required(STACKING_POLICY));
        configs.put(THRESHOLD_BASE, required(THRESHOLD_BASE));

        BigDecimal shippingFee = nonNegativeMoney(configs.get(SHIPPING_FEE));
        BigDecimal freeThreshold = nonNegativeMoney(configs.get(FREE_THRESHOLD));
        BigDecimal taxRate = decimal(configs.get(TAX_RATE), TAX_RATE);
        if (taxRate.signum() < 0 || taxRate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw invalid(TAX_RATE);
        }

        requireValue(configs.get(TAXABLE_BASE), "PRODUCT_PRINTING_AFTER_DISCOUNTS", TAXABLE_BASE);
        requireValue(configs.get(ROUNDING_MODE), "WHOLE_VND_FINAL_TOTAL", ROUNDING_MODE);
        requireValue(configs.get(STACKING_POLICY), "TIER_THEN_COUPON", STACKING_POLICY);
        requireValue(configs.get(THRESHOLD_BASE), "MERCHANDISE_AFTER_DISCOUNTS_BEFORE_TAX", THRESHOLD_BASE);

        boolean shippingActive = booleanValue(configs.get(SHIPPING_ACTIVE));
        Map<String, Long> versions = configs.entrySet().stream()
                .collect(LinkedHashMap::new,
                        (result, entry) -> result.put(entry.getKey(), version(entry.getValue())),
                        Map::putAll);
        return new PricingSettings(shippingFee, freeThreshold, shippingActive, taxRate, versions);
    }

    private PublicConfig required(String key) {
        PublicConfig config = publicConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> unavailable(key));
        if (!config.isActive() || config.getScope() != SettingScope.INTERNAL
                || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            throw unavailable(key);
        }
        return config;
    }

    private BigDecimal nonNegativeMoney(PublicConfig config) {
        BigDecimal value = decimal(config, config.getConfigKey());
        if (value.signum() < 0 || value.stripTrailingZeros().scale() > 0) {
            throw invalid(config.getConfigKey());
        }
        return value;
    }

    private BigDecimal decimal(PublicConfig config, String key) {
        try {
            return new BigDecimal(config.getConfigValue());
        } catch (NumberFormatException exception) {
            throw invalid(key);
        }
    }

    private boolean booleanValue(PublicConfig config) {
        if (!"true".equalsIgnoreCase(config.getConfigValue())
                && !"false".equalsIgnoreCase(config.getConfigValue())) {
            throw invalid(config.getConfigKey());
        }
        return Boolean.parseBoolean(config.getConfigValue());
    }

    private void requireValue(PublicConfig config, String expected, String key) {
        if (!expected.equals(config.getConfigValue())) {
            throw invalid(key);
        }
    }

    private long version(PublicConfig config) {
        return config.getVersion() == null ? 0L : config.getVersion();
    }

    private BadRequestException unavailable(String key) {
        return new BadRequestException("Required pricing setting is unavailable: " + key);
    }

    private BadRequestException invalid(String key) {
        return new BadRequestException("Invalid pricing setting: " + key);
    }

    public record PricingSettings(
            BigDecimal shippingFee,
            BigDecimal freeShippingThreshold,
            boolean shippingActive,
            BigDecimal taxRate,
            Map<String, Long> ruleVersions) {
    }
}
