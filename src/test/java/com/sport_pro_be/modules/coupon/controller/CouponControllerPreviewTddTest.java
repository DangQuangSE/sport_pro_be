package com.sport_pro_be.modules.coupon.controller;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CouponControllerPreviewTddTest {

    private static final Path CONTROLLER_SOURCE = Path.of(
            "src/main/java/com/sport_pro_be/modules/coupon/controller/CouponController.java");

    @Test
    void customerFacingCouponControllerExistsWithUserAuthorization() throws Exception {
        assertThat(Files.exists(CONTROLLER_SOURCE))
                .as("CouponController.java must exist for the customer-facing preview endpoint")
                .isTrue();

        String code = Files.readString(CONTROLLER_SOURCE);

        assertThat(code).contains("@PreAuthorize(\"hasRole('USER')\")");
        assertThat(code).contains("/api/v1/coupons");
        assertThat(code).containsPattern("@PostMapping\\(\"?/preview\"?\\)");
    }

    @Test
    void previewNeverTouchesUsedCount() throws Exception {
        String code = Files.readString(CONTROLLER_SOURCE);

        assertThat(code).doesNotContain("incrementUsageIfBelowLimit");
        assertThat(code).doesNotContain("setUsedCount");
    }
}
