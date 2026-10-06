package com.sport_pro_be.modules.membership.service;

import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.enums.UserTier;
import com.sport_pro_be.modules.membership.domain.MembershipTier;
import com.sport_pro_be.modules.membership.repository.MembershipTierRepository;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TierServiceTest {

    @Mock
    private MembershipTierRepository membershipTierRepository;

    @Mock
    private UserRepository userRepository;

    private TierService tierService;

    @BeforeEach
    void setUp() {
        tierService = new TierService(membershipTierRepository, userRepository);
    }

    @Test
    void resolvesHighestActiveThresholdAndPersistsRelation() {
        MembershipTier silver = tier("SILVER", "5000000", 2, true);
        User user = user(BigDecimal.valueOf(6000000));
        when(membershipTierRepository.findTopByActiveTrueAndThresholdLessThanEqualOrderByThresholdDesc(user.getTotalSpending()))
                .thenReturn(Optional.of(silver));

        MembershipTier current = tierService.resolveCurrentTier(user);

        assertEquals("SILVER", current.getCode());
        assertEquals(silver, user.getMembershipTier());
        verify(userRepository).save(user);
    }

    @Test
    void computesProgressFromDynamicThresholds() {
        MembershipTier bronze = tier("BRONZE", "0", 1, true);
        MembershipTier silver = tier("SILVER", "5000000", 2, true);
        User user = user(BigDecimal.valueOf(2500000));
        when(membershipTierRepository.findTopByActiveTrueAndThresholdLessThanEqualOrderByThresholdDesc(user.getTotalSpending()))
                .thenReturn(Optional.of(bronze));
        when(membershipTierRepository.findAllByActiveTrueOrderBySortOrderAsc())
                .thenReturn(List.of(bronze, silver));

        var profile = tierService.getMembershipProfile(user);

        assertEquals("BRONZE", profile.currentTier().code());
        assertEquals("SILVER", profile.nextTier().code());
        assertEquals(BigDecimal.valueOf(2500000), profile.amountToNextTier());
        assertEquals(BigDecimal.valueOf(50).setScale(2), profile.progressPercentage());
    }

    @Test
    void rejectsMissingActiveBaseTier() {
        User user = user(BigDecimal.ZERO);
        when(membershipTierRepository.findTopByActiveTrueAndThresholdLessThanEqualOrderByThresholdDesc(any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> tierService.resolveCurrentTier(user));
    }

    @Test
    void mapsCustomTierToNearestLowerLegacyThresholdForRollback() {
        MembershipTier custom = tier("VIP", "6000000", 1000, true);
        User user = user(BigDecimal.valueOf(6000000));
        when(membershipTierRepository.findTopByActiveTrueAndThresholdLessThanEqualOrderByThresholdDesc(user.getTotalSpending()))
                .thenReturn(Optional.of(custom));

        tierService.resolveCurrentTier(user);

        assertEquals(UserTier.SILVER, user.getTier());
    }

    private User user(BigDecimal totalSpending) {
        User user = new User();
        user.setTotalSpending(totalSpending);
        return user;
    }

    private MembershipTier tier(String code, String threshold, int sortOrder, boolean active) {
        return MembershipTier.builder()
                .code(code)
                .displayNames(java.util.Map.of("vi", code, "en", code))
                .descriptions(java.util.Map.of("vi", code, "en", code))
                .threshold(new BigDecimal(threshold))
                .sortOrder(sortOrder)
                .discountPercentage(BigDecimal.ZERO)
                .active(active)
                .build();
    }
}
