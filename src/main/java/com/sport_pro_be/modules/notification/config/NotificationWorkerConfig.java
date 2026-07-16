package com.sport_pro_be.modules.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.random.RandomGenerator;

@Configuration
public class NotificationWorkerConfig {

    @Bean
    Clock notificationClock() {
        return Clock.systemUTC();
    }

    @Bean
    RandomGenerator notificationJitterRandom() {
        return RandomGenerator.getDefault();
    }
}
