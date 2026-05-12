package com.sport_pro_be.modules.coupon.interfaces;

import com.sport_pro_be.modules.coupon.dto.CouponRequest;
import com.sport_pro_be.modules.coupon.dto.CouponResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAdminCouponService {
    CouponResponse createCoupon(CouponRequest request);
    Page<CouponResponse> getAllCoupons(Pageable pageable);
    CouponResponse updateCoupon(Long id, CouponRequest request);
    void deleteCoupon(Long id);
}
