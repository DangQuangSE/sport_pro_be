package com.sport_pro_be.modules.order.dto;

import com.sport_pro_be.modules.order.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Invalid phone number format")
    private String phoneNumber;

    private PaymentMethod paymentMethod;

    private String couponCode;

    private java.util.List<Long> cartItemIds;
}
