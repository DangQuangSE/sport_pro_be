package com.sport_pro_be.modules.membership.service;

import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.enums.UserTier;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import com.sport_pro_be.modules.membership.constant.MembershipMessageConstant;
import com.sport_pro_be.modules.membership.domain.MembershipTier;
import com.sport_pro_be.modules.membership.dto.MembershipProfileResponse;
import com.sport_pro_be.modules.membership.dto.MembershipTierResponse;
import com.sport_pro_be.modules.membership.interfaces.ITierService;
import com.sport_pro_be.modules.membership.repository.MembershipTierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TierService implements ITierService {

    private static final BigDecimal LEGACY_SILVER_THRESHOLD = BigDecimal.valueOf(5_000_000L);
    private static final BigDecimal LEGACY_GOLD_THRESHOLD = BigDecimal.valueOf(15_000_000L);
    private static final BigDecimal LEGACY_PLATINUM_THRESHOLD = BigDecimal.valueOf(30_000_000L);

    private final MembershipTierRepository membershipTierRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void updateUserTier(User user) {
        resolveCurrentTier(user);
    }

    @Override
    @Transactional
    public MembershipTier resolveCurrentTier(User user) {
        BigDecimal spending = user.getTotalSpending() == null
                ? BigDecimal.ZERO
                : user.getTotalSpending();
        MembershipTier selected = membershipTierRepository
                .findTopByActiveTrueAndThresholdLessThanEqualOrderByThresholdDesc(spending)
                .orElseThrow(() -> new IllegalStateException(MembershipMessageConstant.TIER_INVALID_CONFIGURATION));

        Long currentTierId = user.getMembershipTier() == null ? null : user.getMembershipTier().getId();
        boolean sameTier = user.getMembershipTier() != null
                && ((currentTierId != null && selected.getId() != null
                && Objects.equals(currentTierId, selected.getId()))
                || user.getMembershipTier() == selected);
        if (!sameTier) {
            user.setMembershipTier(selected);
            syncLegacyTier(user, selected);
            userRepository.save(user);
        }
        return selected;
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipTier getTierByCode(String code) {
        return membershipTierRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new ResourceNotFoundException(MembershipMessageConstant.TIER_CONFIG_NOT_FOUND));
    }

    @Override
    @Transactional
    public boolean isTierAtLeast(User user, MembershipTier requiredTier) {
        if (requiredTier == null) {
            return true;
        }
        if (!requiredTier.isActive()) {
            return false;
        }
        MembershipTier current = resolveCurrentTier(user);
        return current.getSortOrder() >= requiredTier.getSortOrder();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipTier> getActiveTiers() {
        return membershipTierRepository.findAllByActiveTrueOrderBySortOrderAsc();
    }

    @Override
    @Transactional
    public MembershipProfileResponse getMembershipProfile(User user) {
        MembershipTier current = resolveCurrentTier(user);
        List<MembershipTier> activeTiers = membershipTierRepository.findAllByActiveTrueOrderBySortOrderAsc();
        MembershipTier next = activeTiers.stream()
                .filter(tier -> tier.getThreshold().compareTo(current.getThreshold()) > 0)
                .findFirst()
                .orElse(null);

        BigDecimal spending = user.getTotalSpending() == null
                ? BigDecimal.ZERO
                : user.getTotalSpending();
        BigDecimal amountToNext = BigDecimal.ZERO;
        BigDecimal progress = BigDecimal.valueOf(100);
        if (next != null) {
            amountToNext = next.getThreshold().subtract(spending).max(BigDecimal.ZERO);
            BigDecimal range = next.getThreshold().subtract(current.getThreshold());
            BigDecimal earned = spending.subtract(current.getThreshold()).max(BigDecimal.ZERO);
            progress = range.signum() == 0
                    ? BigDecimal.valueOf(100)
                    : earned.multiply(BigDecimal.valueOf(100))
                            .divide(range, 2, RoundingMode.HALF_UP)
                            .min(BigDecimal.valueOf(100));
        }

        return MembershipProfileResponse.builder()
                .currentTier(toResponse(current))
                .nextTier(next == null ? null : toResponse(next))
                .activeTiers(activeTiers.stream().map(this::toResponse).toList())
                .totalSpending(spending)
                .amountToNextTier(amountToNext)
                .progressPercentage(progress)
                .build();
    }

    @Override
    @Transactional
    public void creditSpending(User user, BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Spending amount must be non-negative");
        }

        User lockedUser = user;
        if (user.getId() != null) {
            lockedUser = userRepository.findByIdForUpdate(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        BigDecimal currentSpending = lockedUser.getTotalSpending() == null
                ? BigDecimal.ZERO
                : lockedUser.getTotalSpending();
        lockedUser.setTotalSpending(currentSpending.add(amount));
        resolveCurrentTier(lockedUser);
    }

    private void syncLegacyTier(User user, MembershipTier selected) {
        try {
            user.setTier(UserTier.valueOf(selected.getCode()));
        } catch (IllegalArgumentException ignored) {
            // A previous release can only read the enum. Map a custom tier to
            // the nearest lower legacy threshold; admin-controlled ordering
            // must not change the rollback representation.
            user.setTier(legacyTierForThreshold(selected.getThreshold()));
        }
    }

    private UserTier legacyTierForThreshold(BigDecimal threshold) {
        if (threshold.compareTo(LEGACY_PLATINUM_THRESHOLD) >= 0) {
            return UserTier.PLATINUM;
        }
        if (threshold.compareTo(LEGACY_GOLD_THRESHOLD) >= 0) {
            return UserTier.GOLD;
        }
        if (threshold.compareTo(LEGACY_SILVER_THRESHOLD) >= 0) {
            return UserTier.SILVER;
        }
        return UserTier.BRONZE;
    }

    private MembershipTierResponse toResponse(MembershipTier tier) {
        return MembershipTierResponse.builder()
                .id(tier.getId())
                .code(tier.getCode())
                .displayNames(tier.getDisplayNames())
                .descriptions(tier.getDescriptions())
                .threshold(tier.getThreshold())
                .sortOrder(tier.getSortOrder())
                .discountPercentage(tier.getDiscountPercentage())
                .freeShipping(tier.isFreeShipping())
                .benefits(tier.getBenefits())
                .active(tier.isActive())
                .version(tier.getVersion())
                .build();
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}
