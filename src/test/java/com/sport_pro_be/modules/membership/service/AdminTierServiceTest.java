package com.sport_pro_be.modules.membership.service;

import com.sport_pro_be.exception.ConflictException;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import com.sport_pro_be.modules.coupon.repository.CouponRepository;
import com.sport_pro_be.modules.membership.domain.MembershipTier;
import com.sport_pro_be.modules.membership.dto.MembershipTierRequest;
import com.sport_pro_be.modules.membership.repository.MembershipTierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTierServiceTest {

    @Mock
    private MembershipTierRepository membershipTierRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CouponRepository couponRepository;

    private AdminTierService service;

    @BeforeEach
    void setUp() {
        service = new AdminTierService(membershipTierRepository, userRepository, couponRepository);
    }

    @Test
    void rejectsDuplicateActiveThreshold() {
        MembershipTier bronze = tier("BRONZE", "0", 1, true);
        MembershipTier silver = tier("SILVER", "5000000", 2, true);
        when(membershipTierRepository.findAllForUpdate()).thenReturn(List.of(bronze, silver));

        MembershipTierRequest request = request("GOLD", "5000000", 3);

        assertThrows(RuntimeException.class, () -> service.createMembershipTier(request));
    }

    @Test
    void rejectsDeleteWhenUserReferencesTier() {
        MembershipTier silver = tier("SILVER", "5000000", 2, true);
        silver.setId(2L);
        silver.setVersion(1L);
        when(membershipTierRepository.findAllForUpdate()).thenReturn(List.of(silver));
        when(userRepository.existsByMembershipTierId(2L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.deleteMembershipTier("SILVER", 1L));
    }

    @Test
    void rejectsStaleUpdateVersion() {
        MembershipTier silver = tier("SILVER", "5000000", 2, true);
        silver.setVersion(2L);
        when(membershipTierRepository.findAllForUpdate()).thenReturn(List.of(silver));

        MembershipTierRequest request = request("SILVER", "5000000", 2);
        request.setExpectedVersion(1L);

        assertThrows(ConflictException.class, () -> service.updateMembershipTier("SILVER", request));
    }

    private MembershipTierRequest request(String code, String threshold, int sortOrder) {
        return MembershipTierRequest.builder()
                .code(code)
                .displayNames(Map.of("vi", code, "en", code))
                .descriptions(Map.of("vi", code, "en", code))
                .threshold(new BigDecimal(threshold))
                .sortOrder(sortOrder)
                .discountPercentage(BigDecimal.ZERO)
                .freeShipping(false)
                .benefits(Map.of())
                .active(true)
                .build();
    }

    private MembershipTier tier(String code, String threshold, int sortOrder, boolean active) {
        return MembershipTier.builder()
                .code(code)
                .displayNames(Map.of("vi", code, "en", code))
                .descriptions(Map.of("vi", code, "en", code))
                .threshold(new BigDecimal(threshold))
                .sortOrder(sortOrder)
                .discountPercentage(BigDecimal.ZERO)
                .active(active)
                .build();
    }
}
