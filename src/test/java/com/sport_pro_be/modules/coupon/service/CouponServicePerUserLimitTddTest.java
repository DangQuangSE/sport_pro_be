package com.sport_pro_be.modules.coupon.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CouponServicePerUserLimitTddTest {

    @Test
    void couponEntityExposesMaxUsagePerUser() throws Exception {
        String code = Files.readString(Path.of(
                "src/main/java/com/sport_pro_be/modules/coupon/domain/Coupon.java"));

        assertThat(code).contains("maxUsagePerUser");
    }

    @Test
    void validateAndGetCouponChecksPerUserLimitBeforeApproving() throws Exception {
        String code = Files.readString(Path.of(
                "src/main/java/com/sport_pro_be/modules/coupon/service/CouponService.java"));

        assertThat(code).contains("getMaxUsagePerUser");
        assertThat(code).contains("USER_USAGE_LIMIT_REACHED");
        assertThat(code).containsAnyOf("countByUserIdAndCouponIdAndStatusNotIn", "countByUserIdAndCouponId");
    }

    @Test
    void perUserLimitMessageConstantExists() throws Exception {
        String code = Files.readString(Path.of(
                "src/main/java/com/sport_pro_be/modules/coupon/constant/CouponMessageConstant.java"));

        assertThat(code).contains("USER_USAGE_LIMIT_REACHED");
    }
}
