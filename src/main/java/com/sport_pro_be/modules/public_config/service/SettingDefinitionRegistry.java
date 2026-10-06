package com.sport_pro_be.modules.public_config.service;

import com.sport_pro_be.modules.public_config.domain.ConfigType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class SettingDefinitionRegistry {

    public static final String PUBLIC_ZALO_LINK = "zalo_link";

    private final Map<String, SettingDefinition> definitions = Map.of(
            "checkout.shipping.standard_fee", number("checkout.shipping.standard_fee", "VND", BigDecimal.ZERO, null, true, true, Set.of()),
            "checkout.shipping.free_threshold", number("checkout.shipping.free_threshold", "VND", BigDecimal.ZERO, null, true, true, Set.of()),
            "checkout.shipping.active", definition("checkout.shipping.active", ConfigType.BOOLEAN, null, null, null, false, Set.of(), true, "values=true|false"),
            "checkout.tax.rate", number("checkout.tax.rate", "PERCENT", BigDecimal.ZERO, BigDecimal.valueOf(100), false, true, Set.of()),
            "checkout.tax.taxable_base", definition("checkout.tax.taxable_base", ConfigType.TEXT, null, null, null, false,
                    Set.of("PRODUCT_PRINTING_AFTER_DISCOUNTS"), true, "allowed=PRODUCT_PRINTING_AFTER_DISCOUNTS"),
            "checkout.money.rounding_mode", definition("checkout.money.rounding_mode", ConfigType.TEXT, null, null, null, false,
                    Set.of("WHOLE_VND_FINAL_TOTAL"), true, "allowed=WHOLE_VND_FINAL_TOTAL"),
            "checkout.discount.stacking_policy", definition("checkout.discount.stacking_policy", ConfigType.TEXT, null, null, null, false,
                    Set.of("TIER_THEN_COUPON"), true, "allowed=TIER_THEN_COUPON"),
            "checkout.shipping.threshold_base", definition("checkout.shipping.threshold_base", ConfigType.TEXT, null, null, null, false,
                    Set.of("MERCHANDISE_AFTER_DISCOUNTS_BEFORE_TAX"), true, "allowed=MERCHANDISE_AFTER_DISCOUNTS_BEFORE_TAX")
    );

    private static SettingDefinition number(
            String key,
            String unit,
            BigDecimal minimum,
            BigDecimal maximum,
            boolean wholeVnd,
            boolean required,
            Set<String> allowedValues) {
        return definition(key, ConfigType.NUMBER, unit, minimum, maximum, wholeVnd, allowedValues, required,
                "min=" + minimum + (maximum == null ? "" : ";max=" + maximum) + (wholeVnd ? ";whole=true" : ""));
    }

    private static SettingDefinition definition(
            String key,
            ConfigType type,
            String unit,
            BigDecimal minimum,
            BigDecimal maximum,
            boolean wholeVnd,
            Set<String> allowedValues,
            boolean required,
            String validationRules) {
        return new SettingDefinition(key, type, "CHECKOUT", unit, minimum, maximum, wholeVnd,
                allowedValues, required, validationRules);
    }

    public Optional<SettingDefinition> find(String key) {
        return Optional.ofNullable(definitions.get(normalize(key)));
    }

    public boolean isPublicContentKey(String key) {
        return PUBLIC_ZALO_LINK.equals(normalize(key));
    }

    private String normalize(String key) {
        return key == null ? "" : key.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
