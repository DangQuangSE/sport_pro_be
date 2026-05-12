package com.sport_pro_be.modules.coupon.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.coupon.domain.Coupon;
import com.sport_pro_be.modules.coupon.dto.CouponRequest;
import com.sport_pro_be.modules.coupon.dto.CouponResponse;
import com.sport_pro_be.modules.coupon.repository.CouponRepository;
import com.sport_pro_be.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CouponAdminController {

    private final CouponRepository couponRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(@RequestBody CouponRequest request) {
        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .requiredTier(request.getRequiredTier())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .usageLimit(request.getUsageLimit())
                .isActive(request.isActive())
                .build();
        
        coupon = couponRepository.save(coupon);
        return ResponseEntity.ok(ApiResponse.of("Coupon created successfully", mapToResponse(coupon)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> getAllCoupons(Pageable pageable) {
        Page<CouponResponse> response = couponRepository.findAll(pageable)
                .map(this::mapToResponse);
        return ResponseEntity.ok(ApiResponse.of("Coupons retrieved successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable Long id,
            @RequestBody CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        
        coupon.setCode(request.getCode());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setRequiredTier(request.getRequiredTier());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setActive(request.isActive());
        
        coupon = couponRepository.save(coupon);
        return ResponseEntity.ok(ApiResponse.of("Coupon updated successfully", mapToResponse(coupon)));
    }

    private CouponResponse mapToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .requiredTier(coupon.getRequiredTier())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .isActive(coupon.isActive())
                .build();
    }
}
