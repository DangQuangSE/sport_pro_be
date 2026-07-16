package com.sport_pro_be.modules.notification.config;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordNotificationPropertiesContractTest {

    private static final String TYPE_NAME =
            "com.sport_pro_be.modules.notification.config.DiscordNotificationProperties";

    @Test
    void exposesValidatedTypedConfigurationWithSafeDisabledDefaults() throws Exception {
        Class<?> type = Class.forName(TYPE_NAME);

        ConfigurationProperties properties = type.getAnnotation(ConfigurationProperties.class);
        assertThat(properties).as("configuration properties annotation").isNotNull();
        assertThat(properties.prefix()).isEqualTo("app.notifications.discord");
        assertThat(type).hasAnnotation(Validated.class);

        Object defaults = type.getDeclaredConstructor().newInstance();
        assertThat(invoke(defaults, "isEnabled")).isEqualTo(false);
        assertThat(invoke(defaults, "getCurrency")).isEqualTo("VND");
        assertThat(invoke(defaults, "getBotToken")).isNull();
    }

    @Test
    void enabledConfigurationRejectsMissingDiscordDestinationsTokenAndAdminUrl() throws Exception {
        Class<?> type = Class.forName(TYPE_NAME);
        Object configuration = type.getDeclaredConstructor().newInstance();
        type.getMethod("setEnabled", boolean.class).invoke(configuration, true);

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            Set<?> violations = factory.getValidator().validate(configuration);
            assertThat(violations)
                    .as("enabled Discord notifications must require token, both channels, and admin URL template")
                    .hasSizeGreaterThanOrEqualTo(4);
        }
    }

    private static Object invoke(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }
}
