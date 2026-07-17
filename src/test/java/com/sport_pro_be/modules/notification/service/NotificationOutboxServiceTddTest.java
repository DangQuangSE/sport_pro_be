package com.sport_pro_be.modules.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sport_pro_be.modules.notification.config.DiscordNotificationProperties;
import com.sport_pro_be.modules.notification.domain.NotificationOutbox;
import com.sport_pro_be.modules.notification.domain.OrderNotificationSnapshot;
import com.sport_pro_be.modules.notification.enums.NotificationStatus;
import com.sport_pro_be.modules.notification.enums.NotificationType;
import com.sport_pro_be.modules.notification.repository.NotificationOutboxRepository;
import com.sport_pro_be.modules.order.domain.Order;
import com.sport_pro_be.modules.order.domain.OrderItem;
import com.sport_pro_be.modules.product.domain.Product;
import com.sport_pro_be.modules.product.domain.ProductVariant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationOutboxServiceTddTest {

    private static final String SERVICE_CLASS =
            "com.sport_pro_be.modules.notification.service.NotificationOutboxService";

    @Test
    void disabledNotificationsDoNotPersistAnOutboxRecord() throws Exception {
        NotificationOutboxRepository repository = mock(NotificationOutboxRepository.class);
        Object service = newService(false, repository);

        enqueue(service, finalizedOrder());

        verifyNoInteractions(repository);
    }

    @Test
    void enabledNotificationsPersistOneDeterministicNonPiiSnapshot() throws Exception {
        NotificationOutboxRepository repository = mock(NotificationOutboxRepository.class);
        when(repository.findByNotificationKey(any())).thenReturn(Optional.empty());
        Object service = newService(true, repository);

        enqueue(service, finalizedOrder());

        ArgumentCaptor<NotificationOutbox> captor = ArgumentCaptor.forClass(NotificationOutbox.class);
        verify(repository).save(captor.capture());
        NotificationOutbox outbox = captor.getValue();
        assertThat(outbox.getNotificationKey()).isEqualTo("NEW_ORDER:42");
        assertThat(outbox.getNotificationType()).isEqualTo(NotificationType.NEW_ORDER);
        assertThat(outbox.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(outbox.getOrder().getId()).isEqualTo(42L);

        OrderNotificationSnapshot snapshot = outbox.getPayloadSnapshot();
        assertThat(snapshot.getOrderId()).isEqualTo(42L);
        assertThat(snapshot.getOrderCode()).isEqualTo("42");
        assertThat(snapshot.getTotalAmount()).isEqualByComparingTo("350000.00");
        assertThat(snapshot.getCurrency()).isEqualTo("VND");
        assertThat(snapshot.getLines()).singleElement().satisfies(line -> {
            assertThat(line.getProductName()).isEqualTo("Ao bong da");
            assertThat(line.getVariant()).isEqualTo("SKU-RED-L");
            assertThat(line.getSize()).isEqualTo("L");
            assertThat(line.getColor()).isEqualTo("Do");
            assertThat(line.getQuantity()).isEqualTo(2);
        });

        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(snapshot);
        assertThat(json).doesNotContain("0901234567", "12 Duong Bi Mat", "phone", "shippingAddress", "user");
    }

    @Test
    void duplicateEnqueueHasNoSecondPersistenceEffect() throws Exception {
        NotificationOutboxRepository repository = mock(NotificationOutboxRepository.class);
        NotificationOutbox existing = new NotificationOutbox();
        when(repository.findByNotificationKey("NEW_ORDER:42"))
                .thenReturn(Optional.empty(), Optional.of(existing));
        Object service = newService(true, repository);
        Order order = finalizedOrder();

        enqueue(service, order);
        enqueue(service, order);

        verify(repository, times(1)).save(any(NotificationOutbox.class));
    }

    private static Object newService(boolean enabled, NotificationOutboxRepository repository) throws Exception {
        Class<?> type = Class.forName(SERVICE_CLASS);
        DiscordNotificationProperties properties = new DiscordNotificationProperties();
        properties.setEnabled(enabled);
        properties.setCurrency("VND");
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 2) {
                constructor.setAccessible(true);
                return constructor.newInstance(repository, properties);
            }
        }
        throw new AssertionError("NotificationOutboxService must inject repository and typed Discord properties");
    }

    private static void enqueue(Object service, Order order) throws Exception {
        Method method = service.getClass().getMethod("enqueueNewOrder", Order.class);
        method.invoke(service, order);
    }

    private static Order finalizedOrder() {
        Product product = Product.builder().name("Ao bong da").build();
        ProductVariant variant = ProductVariant.builder()
                .product(product).sku("SKU-RED-L").size("L").colorOld("Do").build();
        Order order = Order.builder()
                .id(42L)
                .phoneNumber("0901234567")
                .shippingAddress("12 Duong Bi Mat")
                .totalAmount(new BigDecimal("350000.00"))
                .build();
        order.setCreatedAt(LocalDateTime.of(2026, 7, 16, 9, 30));
        OrderItem item = OrderItem.builder()
                .order(order).productVariant(variant).quantity(2).price(new BigDecimal("175000.00")).build();
        order.setItems(List.of(item));
        return order;
    }
}
