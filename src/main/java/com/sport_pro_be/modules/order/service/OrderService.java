package com.sport_pro_be.modules.order.service;

import com.sport_pro_be.modules.order.constant.OrderMessageConstant;
import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.audit.annotation.Loggable;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import com.sport_pro_be.modules.cart.domain.Cart;
import com.sport_pro_be.modules.cart.domain.CartItem;
import com.sport_pro_be.modules.cart.repository.CartRepository;
import com.sport_pro_be.modules.order.domain.Order;
import com.sport_pro_be.modules.order.domain.OrderItem;
import com.sport_pro_be.modules.order.dto.OrderItemResponse;
import com.sport_pro_be.modules.order.dto.OrderRequest;
import com.sport_pro_be.modules.order.dto.OrderResponse;
import com.sport_pro_be.modules.order.enums.OrderStatus;
import com.sport_pro_be.modules.order.interfaces.IOrderService;
import com.sport_pro_be.modules.order.repository.OrderItemRepository;
import com.sport_pro_be.modules.order.repository.OrderRepository;
import com.sport_pro_be.modules.product.domain.ProductVariant;
import com.sport_pro_be.modules.product.domain.ProductImage;
import com.sport_pro_be.modules.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final com.sport_pro_be.modules.coupon.interfaces.ICouponService couponService;
    private final com.sport_pro_be.modules.membership.interfaces.ITierService tierService;

    @Override
    @Transactional
    @Loggable(action = "PLACE_ORDER", module = "ORDER")
    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        log.info("Placing order for user id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(OrderMessageConstant.USER_NOT_FOUND));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException(OrderMessageConstant.CART_EMPTY));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException(OrderMessageConstant.CART_EMPTY);
        }

        List<CartItem> itemsToOrder;
        if (request.getCartItemIds() != null && !request.getCartItemIds().isEmpty()) {
            itemsToOrder = cart.getItems().stream()
                    .filter(item -> request.getCartItemIds().contains(item.getId()))
                    .collect(Collectors.toList());

            if (itemsToOrder.isEmpty()) {
                throw new BadRequestException(OrderMessageConstant.INVALID_SELECTED_ITEMS);
            }
        } else {
            itemsToOrder = new ArrayList<>(cart.getItems());
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .phoneNumber(request.getPhoneNumber())
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .build();

        // Must save order first to satisfy foreign key for OrderItem
        order = orderRepository.save(order);

        for (CartItem cartItem : itemsToOrder) {
            ProductVariant variant = cartItem.getProductVariant();

            if (variant.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException(String.format(OrderMessageConstant.INSUFFICIENT_STOCK,
                        variant.getProduct().getName(), variant.getSize()));
            }

            // Deduct stock
            variant.setStockQuantity(variant.getStockQuantity() - cartItem.getQuantity());
            productVariantRepository.save(variant);

            // Determine product price
            BigDecimal itemPrice = variant.getSalePrice() != null ? variant.getSalePrice() : variant.getOriginalPrice();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            // Add printing price if this item has a custom design (snapshot price stored in
            // design)
            if (cartItem.getCustomDesign() != null) {
                itemTotal = itemTotal.add(cartItem.getCustomDesign().getTotalPrintingPrice());
            }
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productVariant(variant)
                    .quantity(cartItem.getQuantity())
                    .price(itemPrice)
                    .customDesign(cartItem.getCustomDesign())
                    .build();

            orderItems.add(orderItem);
        }

        orderItemRepository.saveAll(orderItems);

        // Handle Coupon
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            com.sport_pro_be.modules.coupon.domain.Coupon coupon = couponService
                    .validateAndGetCoupon(request.getCouponCode(), user, totalAmount);
            discountAmount = couponService.calculateDiscount(coupon, totalAmount);
            order.setCoupon(coupon);
            coupon.setUsedCount(coupon.getUsedCount() + 1);
        }

        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount.subtract(discountAmount));
        order.setItems(orderItems);
        orderRepository.save(order);

        // Clear only ordered items from cart
        cart.getItems().removeAll(itemsToOrder);
        cartRepository.save(cart);

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(this::mapToOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(OrderMessageConstant.ORDER_NOT_FOUND));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(String search, OrderStatus status, Pageable pageable) {
        String cleanSearch = (search == null || search.trim().isEmpty()) ? null : search.trim();
        return orderRepository.searchOrders(cleanSearch, status, pageable).map(this::mapToOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetailsAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(OrderMessageConstant.ORDER_NOT_FOUND));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional
    @Loggable(action = "UPDATE_ORDER_STATUS", module = "ORDER")
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(OrderMessageConstant.ORDER_NOT_FOUND));

        if (status == OrderStatus.DELIVERED && order.getStatus() != OrderStatus.DELIVERED) {
            User user = order.getUser();
            user.setTotalSpending(user.getTotalSpending().add(order.getTotalAmount()));
            tierService.updateUserTier(user);
            userRepository.save(user);
        }

        order.setStatus(status);
        orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> {
                    OrderItemResponse.OrderItemResponseBuilder builder = OrderItemResponse.builder()
                            .id(item.getId())
                            .productVariantId(item.getProductVariant().getId())
                            .productName(item.getProductVariant().getProduct().getName())
                            .sku(item.getProductVariant().getSku())
                            .size(item.getProductVariant().getSize())
                            .color(item.getProductVariant().getColor() != null
                                    ? item.getProductVariant().getColor().getName()
                                    : item.getProductVariant().getColorOld())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .isReviewed(item.getReview() != null);

                    if (item.getCustomDesign() != null) {
                        builder.customDesignId(item.getCustomDesign().getId())
                                .designImageUrl(item.getCustomDesign().getDesignImageUrl())
                                .backDesignImageUrl(item.getCustomDesign().getBackDesignImageUrl())
                                .printingPrice(item.getCustomDesign().getTotalPrintingPrice());
                    } else {
                        String defaultImageUrl = item.getProductVariant().getProduct().getImages().stream()
                                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                                .map(ProductImage::getImageUrl)
                                .findFirst()
                                .orElseGet(() -> item.getProductVariant().getProduct().getImages().stream()
                                        .map(ProductImage::getImageUrl)
                                        .findFirst()
                                        .orElse(null));
                        builder.designImageUrl(defaultImageUrl);
                    }
                    return builder.build();
                })
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .shippingAddress(order.getShippingAddress())
                .phoneNumber(order.getPhoneNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}
