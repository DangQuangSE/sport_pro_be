package com.sport_pro_be.modules.coupon.repository;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CouponRepositoryAtomicIncrementTddTest {

    @Test
    void repositoryExposesAnAtomicConditionalIncrement() throws Exception {
        String code = Files.readString(Path.of(
                "src/main/java/com/sport_pro_be/modules/coupon/repository/CouponRepository.java"));

        assertThat(code).contains("@Modifying");
        assertThat(code).contains("incrementUsageIfBelowLimit");
        // Must be a conditional UPDATE, not an unconditional one - the whole point is
        // that a coupon at/over its usageLimit does not get incremented further.
        assertThat(code).containsPattern("usageLimit\\s+IS\\s+NULL|usedCount\\s*<\\s*.*usageLimit");
    }

    @Test
    void orderServiceNoLongerMutatesUsedCountInMemory() throws Exception {
        String code = Files.readString(Path.of(
                "src/main/java/com/sport_pro_be/modules/order/service/OrderService.java"));

        // The old unsafe read-modify-write must be gone...
        assertThat(code).doesNotContain("coupon.setUsedCount(coupon.getUsedCount() + 1)");
        // ...replaced by a call through ICouponService (which wraps the atomic repository
        // method), keeping OrderService decoupled from CouponRepository internals...
        assertThat(code).contains("couponService.incrementUsage(");
        // ...and a zero-rows-affected result must be treated as limit-reached, not ignored.
        assertThat(code).contains("USAGE_LIMIT_REACHED");
    }
}
