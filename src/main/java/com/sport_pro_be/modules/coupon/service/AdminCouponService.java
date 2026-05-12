package com.sport_pro_be.modules.coupon.service;

import com.sport_pro_be.modules.coupon.constant.CouponMessageConstant;
import com.sport_pro_be.modules.coupon.domain.Coupon;
import com.sport_pro_be.modules.coupon.dto.CouponRequest;
import com.sport_pro_be.modules.coupon.dto.CouponResponse;
import com.sport_pro_be.modules.coupon.interfaces.IAdminCouponService;
import com.sport_pro_be.modules.coupon.repository.CouponRepository;
import com.sport_pro_be.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCouponService implements IAdminCouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
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
        return mapToResponse(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CouponResponse> getAllCoupons(Pageable pageable) {
        return couponRepository.findAllByIsDeletedFalse(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(CouponMessageConstant.COUPON_NOT_FOUND));
        
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
        return mapToResponse(coupon);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(CouponMessageConstant.COUPON_NOT_FOUND));
        coupon.setDeleted(true);
        coupon.setActive(false);
        couponRepository.save(coupon);
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
