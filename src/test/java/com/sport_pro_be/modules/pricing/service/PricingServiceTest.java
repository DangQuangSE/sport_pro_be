package com.sport_pro_be.modules.pricing.service;

import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import com.sport_pro_be.modules.cart.domain.Cart;
import com.sport_pro_be.modules.cart.domain.CartItem;
import com.sport_pro_be.modules.cart.repository.CartRepository;
import com.sport_pro_be.modules.coupon.domain.Coupon;
import com.sport_pro_be.modules.coupon.interfaces.ICouponService;
import com.sport_pro_be.modules.membership.domain.MembershipTier;
import com.sport_pro_be.modules.membership.interfaces.ITierService;
import com.sport_pro_be.modules.pricing.dto.PricingBreakdownResponse;
import com.sport_pro_be.modules.product.domain.Product;
import com.sport_pro_be.modules.product.domain.ProductVariant;
import com.sport_pro_be.modules.product.enums.Gender;
import com.sport_pro_be.modules.product.enums.ProductStatus;
import com.sport_pro_be.modules.public_config.domain.ConfigType;
import com.sport_pro_be.modules.public_config.domain.PublicConfig;
import com.sport_pro_be.modules.public_config.domain.SettingScope;
import com.sport_pro_be.modules.public_config.repository.PublicConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ICouponService couponService;

    @Mock
    private ITierService tierService;

    @Mock
    private PublicConfigRepository publicConfigRepository;

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(
                cartRepository,
                userRepository,
                couponService,
                tierService,
                new PricingSettingsReader(publicConfigRepository));
    }

    @Test
    void calculatesTierCouponShippingTaxAndExactWholeVndSnapshot() {
        User user = new User();
        Product product = Product.builder()
                .name("Training shirt")
                .status(ProductStatus.ACTIVE)
                .gender(Gender.UNISEX)
                .build();
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .originalPrice(new BigDecimal("1000"))
                .stockQuantity(10)
                .status(ProductStatus.ACTIVE)
                .build();
        CartItem item = CartItem.builder().id(10L).productVariant(variant).quantity(1).build();
        Cart cart = Cart.builder().items(List.of(item)).build();
        when(cartRepository.findByUserId(null)).thenReturn(Optional.of(cart));

        MembershipTier tier = MembershipTier.builder()
                .code("SILVER")
                .threshold(BigDecimal.ZERO)
                .sortOrder(2)
                .discountPercentage(new BigDecimal("10"))
                .freeShipping(false)
                .version(3L)
                .active(true)
                .build();
        when(tierService.resolveCurrentTier(user)).thenReturn(tier);

        Coupon coupon = Coupon.builder().code("SAVE").build();
        when(couponService.validateAndGetCoupon(eq("SAVE"), eq(user), any(BigDecimal.class)))
                .thenReturn(coupon);
        when(couponService.calculateDiscount(eq(coupon), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("100"));
        stubPricingSettings();

        PricingBreakdownResponse result = pricingService.calculate(user, List.of(10L), "SAVE").breakdown();

        assertEquals(new BigDecimal("1000"), result.subtotalAmount());
        assertEquals(BigDecimal.ZERO.setScale(0), result.printingAmount());
        assertEquals(new BigDecimal("100"), result.tierDiscountAmount());
        assertEquals(new BigDecimal("100"), result.couponDiscountAmount());
        assertEquals(new BigDecimal("15"), result.shippingAmount());
        assertEquals(new BigDecimal("80"), result.taxAmount());
        assertEquals(new BigDecimal("895"), result.totalAmount());
        assertEquals(new BigDecimal("895"), result.subtotalAmount()
                .add(result.printingAmount())
                .subtract(result.tierDiscountAmount())
                .subtract(result.couponDiscountAmount())
                .add(result.shippingAmount())
                .add(result.taxAmount()));
        assertEquals(3L, result.ruleVersions().get("membership.tier.SILVER"));
    }

    @Test
    void refusesToPriceWhenARequiredSettingIsMissing() {
        User user = new User();
        CartItem item = CartItem.builder()
                .productVariant(ProductVariant.builder()
                        .product(Product.builder().status(ProductStatus.ACTIVE).gender(Gender.UNISEX).build())
                        .originalPrice(new BigDecimal("1000"))
                        .stockQuantity(1)
                        .status(ProductStatus.ACTIVE)
                        .build())
                .quantity(1)
                .build();
        when(cartRepository.findByUserId(null)).thenReturn(Optional.of(Cart.builder().items(List.of(item)).build()));
        when(publicConfigRepository.findByConfigKey(any(String.class))).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> pricingService.calculate(user, null, null));
    }

    private void stubPricingSettings() {
        when(publicConfigRepository.findByConfigKey(any(String.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            Map<String, String> values = Map.of(
                    "checkout.shipping.standard_fee", "15",
                    "checkout.shipping.free_threshold", "5000",
                    "checkout.shipping.active", "true",
                    "checkout.tax.rate", "10",
                    "checkout.tax.taxable_base", "PRODUCT_PRINTING_AFTER_DISCOUNTS",
                    "checkout.money.rounding_mode", "WHOLE_VND_FINAL_TOTAL",
                    "checkout.discount.stacking_policy", "TIER_THEN_COUPON",
                    "checkout.shipping.threshold_base", "MERCHANDISE_AFTER_DISCOUNTS_BEFORE_TAX");
            return Optional.of(PublicConfig.builder()
                    .configKey(key)
                    .configValue(values.get(key))
                    .configType(key.endsWith("active") ? ConfigType.BOOLEAN
                            : key.contains("rate") || key.contains("fee") || key.contains("threshold")
                            ? ConfigType.NUMBER : ConfigType.TEXT)
                    .scope(SettingScope.INTERNAL)
                    .active(true)
                    .version(2L)
                    .build());
        });
    }
}
