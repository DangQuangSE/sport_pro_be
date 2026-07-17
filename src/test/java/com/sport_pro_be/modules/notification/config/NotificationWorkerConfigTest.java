package com.sport_pro_be.modules.notification.config;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationWorkerConfigTest {

    @Test
    void jitterUsesRandomImplementationAvailableInMinimalJavaRuntime() {
        assertThat(new NotificationWorkerConfig().notificationJitterRandom())
                .isInstanceOf(Random.class);
    }
}
