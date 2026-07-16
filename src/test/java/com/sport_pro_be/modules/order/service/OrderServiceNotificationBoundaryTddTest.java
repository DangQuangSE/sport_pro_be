package com.sport_pro_be.modules.order.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceNotificationBoundaryTddTest {

    @Test
    void checkoutEnqueuesAfterFinalOrderAndCartPersistenceWithoutDiscordTransport() throws Exception {
        Path source = Path.of("src/main/java/com/sport_pro_be/modules/order/service/OrderService.java");
        String code = Files.readString(source);

        assertThat(code).contains("NotificationOutboxService");
        assertThat(code).contains("notificationOutboxService.enqueueNewOrder(order)");
        assertThat(code.indexOf("cartRepository.save(cart)"))
                .isLessThan(code.indexOf("notificationOutboxService.enqueueNewOrder(order)"));
        assertThat(code).doesNotContain("DiscordClient", "DiscordNotificationClient", "@Async");
    }
}
