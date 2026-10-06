package com.sport_pro_be.modules.pricing.service;

import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.exception.ResourceNotFoundException;
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
import com.sport_pro_be.modules.product.enums.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PricingService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final String CURRENCY = "VND";

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ICouponService couponService;
    private final ITierService tierService;
    private final PricingSettingsReader settingsReader;

    @Transactional
    public PricingCalculation calculate(Long userId, List<Long> selectedCartItemIds, String couponCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return calculate(user, selectedCartItemIds, couponCode);
    }

    @Transactional
    public PricingCalculation calculate(User user, List<Long> selectedCartItemIds, String couponCode) {
        List<CartItem> cartItems = selectCartItems(user.getId(), selectedCartItemIds);
        PricingSettingsReader.PricingSettings settings = settingsReader.read();
        MembershipTier tier = tierService.resolveCurrentTier(user);
        List<PricingLine> lines = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal printing = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            validateLine(item);
            BigDecimal unitPrice = effectivePrice(item);
            BigDecimal printingAmount = printingPrice(item);
            lines.add(new PricingLine(item, unitPrice, printingAmount));
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            printing = printing.add(printingAmount);
        }

        BigDecimal merchandiseSubtotal = subtotal.add(printing);
        BigDecimal tierDiscount = percentageDiscount(merchandiseSubtotal, tier.getDiscountPercentage());
        BigDecimal afterTierDiscount = merchandiseSubtotal.subtract(tierDiscount).max(BigDecimal.ZERO);

        Coupon coupon = null;
        BigDecimal couponDiscount = BigDecimal.ZERO;
        if (couponCode != null && !couponCode.isBlank()) {
            coupon = couponService.validateAndGetCoupon(couponCode.trim(), user, merchandiseSubtotal);
            couponDiscount = couponService.calculateDiscount(coupon, afterTierDiscount);
            if (couponDiscount == null || couponDiscount.signum() < 0) {
                throw new BadRequestException("Coupon discount is invalid");
            }
        }
        BigDecimal afterDiscounts = afterTierDiscount.subtract(couponDiscount).max(BigDecimal.ZERO);

        BigDecimal shipping = BigDecimal.ZERO;
        if (settings.shippingActive()
                && !tier.isFreeShipping()
                && afterDiscounts.compareTo(settings.freeShippingThreshold()) < 0) {
            shipping = settings.shippingFee();
        }

        BigDecimal tax = afterDiscounts.multiply(settings.taxRate()).divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP);
        BigDecimal rawTotal = afterDiscounts.add(shipping).add(tax);

        BigDecimal subtotalSnapshot = whole(subtotal);
        BigDecimal printingSnapshot = whole(printing);
        BigDecimal tierDiscountSnapshot = whole(tierDiscount);
        BigDecimal couponDiscountSnapshot = whole(couponDiscount);
        BigDecimal shippingSnapshot = whole(shipping);
        BigDecimal totalSnapshot = whole(rawTotal);
        BigDecimal taxSnapshot = totalSnapshot
                .subtract(subtotalSnapshot)
                .subtract(printingSnapshot)
                .add(tierDiscountSnapshot)
                .add(couponDiscountSnapshot)
                .subtract(shippingSnapshot);
        if (taxSnapshot.signum() < 0) {
            throw new BadRequestException("Pricing calculation cannot produce a valid whole-VND snapshot");
        }

        Map<String, Long> ruleVersions = new LinkedHashMap<>(settings.ruleVersions());
        if (tier.getVersion() != null && tier.getCode() != null) {
            ruleVersions.put("membership.tier." + tier.getCode(), tier.getVersion());
        }

        PricingBreakdownResponse breakdown = PricingBreakdownResponse.builder()
                .subtotalAmount(subtotalSnapshot)
                .printingAmount(printingSnapshot)
                .tierDiscountAmount(tierDiscountSnapshot)
                .couponDiscountAmount(couponDiscountSnapshot)
                .shippingAmount(shippingSnapshot)
                .taxAmount(taxSnapshot)
                .totalAmount(totalSnapshot)
                .currency(CURRENCY)
                .appliedTierCode(tier.getCode())
                .ruleVersions(Collections.unmodifiableMap(ruleVersions))
                .build();
        return new PricingCalculation(List.copyOf(lines), coupon, tier, breakdown);
    }

    private List<CartItem> selectCartItems(Long userId, List<Long> selectedCartItemIds) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        if (selectedCartItemIds == null || selectedCartItemIds.isEmpty()) {
            return new ArrayList<>(cart.getItems());
        }
        List<CartItem> selected = cart.getItems().stream()
                .filter(item -> selectedCartItemIds.contains(item.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        if (selected.isEmpty() || selected.size() != selectedCartItemIds.stream().distinct().count()) {
            throw new BadRequestException("No valid items selected from cart");
        }
        return selected;
    }

    private void validateLine(CartItem item) {
        if (item.getProductVariant() == null
                || item.getProductVariant().getStatus() != ProductStatus.ACTIVE
                || item.getProductVariant().getProduct() == null
                || item.getProductVariant().getProduct().getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("A selected product is no longer available");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new BadRequestException("Cart quantity must be positive");
        }
        if (item.getProductVariant().getStockQuantity() == null
                || item.getProductVariant().getStockQuantity() < item.getQuantity()) {
            throw new BadRequestException("Insufficient stock for product: "
                    + item.getProductVariant().getProduct().getName());
        }
    }

    private BigDecimal effectivePrice(CartItem item) {
        BigDecimal price = item.getProductVariant().getSalePrice() != null
                ? item.getProductVariant().getSalePrice()
                : item.getProductVariant().getOriginalPrice();
        if (price == null || price.signum() < 0) {
            throw new BadRequestException("Product price is invalid");
        }
        return price;
    }

    private BigDecimal printingPrice(CartItem item) {
        if (item.getCustomDesign() == null || item.getCustomDesign().getTotalPrintingPrice() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal price = item.getCustomDesign().getTotalPrintingPrice();
        if (price.signum() < 0) {
            throw new BadRequestException("Printing price is invalid");
        }
        return price;
    }

    private BigDecimal percentageDiscount(BigDecimal amount, BigDecimal percentage) {
        if (percentage == null || percentage.signum() < 0 || percentage.compareTo(ONE_HUNDRED) > 0) {
            throw new BadRequestException("Membership discount is invalid");
        }
        return amount.multiply(percentage).divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP)
                .min(amount)
                .max(BigDecimal.ZERO);
    }

    private BigDecimal whole(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP);
    }
}
