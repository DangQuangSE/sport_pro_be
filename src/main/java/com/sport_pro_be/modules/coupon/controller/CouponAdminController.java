package com.sport_pro_be.modules.coupon.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.coupon.constant.CouponMessageConstant;
import com.sport_pro_be.modules.coupon.dto.CouponRequest;
import com.sport_pro_be.modules.coupon.dto.CouponResponse;
import com.sport_pro_be.modules.coupon.interfaces.IAdminCouponService;
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

    private final IAdminCouponService adminCouponService;

    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(@RequestBody CouponRequest request) {
        CouponResponse response = adminCouponService.createCoupon(request);
        return ResponseEntity.ok(ApiResponse.of(CouponMessageConstant.COUPON_CREATED, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CouponResponse>>> getAllCoupons(Pageable pageable) {
        Page<CouponResponse> response = adminCouponService.getAllCoupons(pageable);
        return ResponseEntity.ok(ApiResponse.of(CouponMessageConstant.COUPONS_RETRIEVED, response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable Long id,
            @RequestBody CouponRequest request) {
        CouponResponse response = adminCouponService.updateCoupon(id, request);
        return ResponseEntity.ok(ApiResponse.of(CouponMessageConstant.COUPON_UPDATED, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        adminCouponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.of(CouponMessageConstant.COUPON_DELETED, null));
    }
}
