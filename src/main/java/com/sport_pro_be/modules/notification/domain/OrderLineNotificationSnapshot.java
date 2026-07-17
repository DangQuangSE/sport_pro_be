package com.sport_pro_be.modules.notification.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.PRODUCT_NAME_REQUIRED;
import static com.sport_pro_be.modules.notification.constant.NotificationMessageConstant.QUANTITY_MIN;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineNotificationSnapshot {

    @NotBlank(message = PRODUCT_NAME_REQUIRED)
    private String productName;

    private String variant;

    private String size;

    private String color;

    @Min(value = 1, message = QUANTITY_MIN)
    private int quantity;
}
