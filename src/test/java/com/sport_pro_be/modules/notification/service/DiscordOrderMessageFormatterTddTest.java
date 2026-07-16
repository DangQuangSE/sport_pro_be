package com.sport_pro_be.modules.notification.service;

import com.sport_pro_be.modules.notification.config.DiscordNotificationProperties;
import com.sport_pro_be.modules.notification.domain.OrderLineNotificationSnapshot;
import com.sport_pro_be.modules.notification.domain.OrderNotificationSnapshot;
import com.sport_pro_be.modules.notification.dto.DiscordMessageRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordOrderMessageFormatterTddTest {

    @Test
    void formatsOnlyTheTypedSnapshotWithSafeOrderDetailsAndAdminLink() {
        DiscordOrderMessageFormatter formatter = formatter();
        OrderNotificationSnapshot snapshot = new OrderNotificationSnapshot(
                1,
                42L,
                "42",
                Instant.parse("2026-07-16T02:30:00Z"),
                new BigDecimal("350000.00"),
                "VND",
                List.of(new OrderLineNotificationSnapshot(
                        "Ao **VIP** @everyone <@&123456789>", "SKU_RED", "L", "Do", 2)));

        List<DiscordMessageRequest> messages = formatter.format(snapshot);

        assertThat(messages).singleElement().satisfies(request -> {
            assertThat(request.content())
                    .contains("#42", "Ao \\*\\*VIP\\*\\* @\u200Beveryone", "<@\u200B&123456789>")
                    .contains("SKU\\_RED", "L", "Do", "2", "350.000 VND")
                    .contains("2026-07-16", "https://admin.sport-pro.test/vi/admin/orders/42")
                    .doesNotContain("customer", "phone", "address", "token");
            assertThat(request.allowedMentions().parse()).isEmpty();
        });
    }

    @Test
    void createsDeterministicNumberedChunksNoLongerThanDiscordContentLimit() {
        DiscordOrderMessageFormatter formatter = formatter();
        List<OrderLineNotificationSnapshot> lines = new ArrayList<>();
        for (int index = 1; index <= 35; index++) {
            lines.add(new OrderLineNotificationSnapshot(
                    "San pham " + index + " " + "x".repeat(90), null, null, null, index));
        }
        OrderNotificationSnapshot snapshot = new OrderNotificationSnapshot(
                1, 77L, "77", Instant.parse("2026-07-16T02:30:00Z"),
                new BigDecimal("1234567"), "VND", lines);

        List<DiscordMessageRequest> first = formatter.format(snapshot);
        List<DiscordMessageRequest> second = formatter.format(snapshot);

        assertThat(first).hasSizeGreaterThan(1).isEqualTo(second);
        for (int index = 0; index < first.size(); index++) {
            assertThat(first.get(index).content())
                    .hasSizeLessThanOrEqualTo(2000)
                    .contains("#77", "Part " + (index + 1) + "/" + first.size());
            assertThat(first.get(index).allowedMentions().parse()).isEmpty();
        }
    }

    private static DiscordOrderMessageFormatter formatter() {
        DiscordNotificationProperties properties = new DiscordNotificationProperties();
        properties.setAdminOrderUrlTemplate("https://admin.sport-pro.test/vi/admin/orders/{orderId}");
        properties.setCurrency("VND");
        return new DiscordOrderMessageFormatter(properties);
    }
}
