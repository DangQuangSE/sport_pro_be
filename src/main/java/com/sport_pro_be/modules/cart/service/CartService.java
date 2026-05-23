package com.sport_pro_be.modules.cart.service;

import com.sport_pro_be.modules.auth.constant.AuthConstant;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.cart.constant.CartMessageConstant;
import com.sport_pro_be.modules.cart.domain.Cart;
import com.sport_pro_be.modules.cart.domain.CartItem;
import com.sport_pro_be.modules.cart.dto.request.CartItemRequest;
import com.sport_pro_be.modules.cart.dto.response.CartItemResponse;
import com.sport_pro_be.modules.cart.dto.response.CartResponse;
import com.sport_pro_be.modules.cart.interfaces.ICartService;
import com.sport_pro_be.modules.cart.repository.CartRepository;
import com.sport_pro_be.modules.custom_design.domain.CustomDesign;
import com.sport_pro_be.modules.custom_design.interfaces.ICustomDesignService;
import com.sport_pro_be.modules.product.domain.ProductVariant;
import com.sport_pro_be.modules.product.enums.ProductStatus;
import com.sport_pro_be.modules.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {

    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final ICustomDesignService customDesignService;

    @Override
    @Transactional
    public CartResponse getMyCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addOrUpdateItem(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(CartMessageConstant.VARIANT_NOT_FOUND));

        if (variant.getStatus() != ProductStatus.ACTIVE) {
            throw new ResourceNotFoundException(CartMessageConstant.VARIANT_NOT_FOUND);
        }

        // Resolve custom design (and verify ownership at the same time)
        CustomDesign customDesign = null;
        if (request.getCustomDesignId() != null) {
            customDesign = customDesignService.findAndVerifyOwnership(userId, request.getCustomDesignId());
        }

        final CustomDesign resolvedDesign = customDesign;

        // Find existing item that matches the same variant AND design combination
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductVariant().getId().equals(request.getVariantId())
                        && isSameDesign(item.getCustomDesign(), resolvedDesign))
                .findFirst();

        int currentQuantity = existingItemOpt.map(CartItem::getQuantity).orElse(0);
        int newQuantity = currentQuantity + request.getQuantity();

        if (newQuantity > variant.getStockQuantity()) {
            throw new BadRequestException(CartMessageConstant.OUT_OF_STOCK);
        }

        if (newQuantity <= 0) {
            existingItemOpt.ifPresent(item -> cart.getItems().remove(item));
        } else {
            if (existingItemOpt.isPresent()) {
                existingItemOpt.get().setQuantity(newQuantity);
            } else {
                CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .productVariant(variant)
                        .quantity(newQuantity)
                        .customDesign(resolvedDesign)
                        .build();
                cart.getItems().add(newItem);
            }
        }

        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        if (!removed) {
            throw new ResourceNotFoundException(CartMessageConstant.CART_ITEM_NOT_FOUND);
        }
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(AuthConstant.ACCOUNT_NOT_FOUND));
            Cart newCart = Cart.builder()
                    .user(user)
                    .items(new ArrayList<>())
                    .build();
            return cartRepository.save(newCart);
        });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;
        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            ProductVariant variant = item.getProductVariant();
            BigDecimal activePrice = variant.getSalePrice() != null ? variant.getSalePrice() : variant.getOriginalPrice();
            BigDecimal itemProductTotal = activePrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            // Add printing price if this item has a custom design
            BigDecimal printingPrice = BigDecimal.ZERO;
            if (item.getCustomDesign() != null) {
                printingPrice = item.getCustomDesign().getTotalPrintingPrice();
            }
            BigDecimal itemTotal = itemProductTotal.add(printingPrice);

            totalAmount = totalAmount.add(itemTotal);
            totalItems += item.getQuantity();

            CartItemResponse.CartItemResponseBuilder responseBuilder = CartItemResponse.builder()
                    .id(item.getId())
                    .variantId(variant.getId())
                    .productName(variant.getProduct().getName())
                    .productSlug(variant.getProduct().getSlug())
                    .size(variant.getSize())
                    .color(variant.getColor() != null ? variant.getColor().getName() : variant.getColorOld())
                    .originalPrice(variant.getOriginalPrice())
                    .salePrice(variant.getSalePrice())
                    .quantity(item.getQuantity())
                    .itemTotal(itemTotal);

            if (item.getCustomDesign() != null) {
                responseBuilder
                        .customDesignId(item.getCustomDesign().getId())
                        .designImageUrl(item.getCustomDesign().getDesignImageUrl())
                        .printingPrice(printingPrice);
            }

            itemResponses.add(responseBuilder.build());
        }

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }

    /**
     * Checks if two CustomDesign references refer to the same design (both null or same ID).
     */
    private boolean isSameDesign(CustomDesign a, CustomDesign b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.getId().equals(b.getId());
    }
}
