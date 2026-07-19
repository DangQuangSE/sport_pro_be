package com.sport_pro_be.modules.coupon.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.common.SecurityUtils;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.coupon.domain.Coupon;
import com.sport_pro_be.modules.coupon.dto.CouponPreviewRequest;
import com.sport_pro_be.modules.coupon.dto.CouponPreviewResponse;
import com.sport_pro_be.modules.coupon.interfaces.ICouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CouponController {

    private final ICouponService couponService;

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<CouponPreviewResponse>> previewCoupon(@Valid @RequestBody CouponPreviewRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        BigDecimal orderAmount = request.getOrderAmount();

        Coupon coupon = couponService.validateAndGetCoupon(request.getCode(), currentUser, orderAmount);
        BigDecimal discountAmount = couponService.calculateDiscount(coupon, orderAmount);

        CouponPreviewResponse response = CouponPreviewResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .discountAmount(discountAmount)
                .finalAmount(orderAmount.subtract(discountAmount))
                .build();

        return ResponseEntity.ok(ApiResponse.of(null, response));
    }
}
