package com.sport_pro_be.modules.membership.service;

import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.exception.ConflictException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.auth.enums.UserTier;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import com.sport_pro_be.modules.coupon.repository.CouponRepository;
import com.sport_pro_be.modules.membership.constant.MembershipMessageConstant;
import com.sport_pro_be.modules.membership.domain.MembershipTier;
import com.sport_pro_be.modules.membership.dto.MembershipTierRequest;
import com.sport_pro_be.modules.membership.dto.MembershipTierResponse;
import com.sport_pro_be.modules.membership.dto.TierConfigRequest;
import com.sport_pro_be.modules.membership.dto.TierConfigResponse;
import com.sport_pro_be.modules.membership.interfaces.IAdminTierService;
import com.sport_pro_be.modules.membership.repository.MembershipTierRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminTierService implements IAdminTierService {

    private final MembershipTierRepository membershipTierRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TierConfigResponse> getAllTierConfigs() {
        return membershipTierRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::mapToLegacyResponse)
                .toList();
    }

    @Override
    @Transactional
    public TierConfigResponse updateTierConfig(Long id, TierConfigRequest request) {
        MembershipTier current = findById(id);
        MembershipTierRequest dynamicRequest = MembershipTierRequest.builder()
                .displayNames(current.getDisplayNames())
                .descriptions(current.getDescriptions())
                .threshold(request.getThreshold())
                .sortOrder(current.getSortOrder())
                .discountPercentage(current.getDiscountPercentage())
                .freeShipping(current.isFreeShipping())
                .benefits(current.getBenefits())
                .active(current.isActive())
                .expectedVersion(request.getExpectedVersion())
                .build();
        if (request.getDescription() != null) {
            Map<String, String> descriptions = new HashMap<>(current.getDescriptions());
            descriptions.put("vi", request.getDescription());
            descriptions.putIfAbsent("en", request.getDescription());
            dynamicRequest.setDescriptions(descriptions);
        }
        return mapToLegacyResponse(updateTier(current.getCode(), dynamicRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipTierResponse> getMembershipTiers(Boolean active) {
        return membershipTierRepository.findAllByOrderBySortOrderAsc().stream()
                .filter(tier -> active == null || tier.isActive() == active)
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public MembershipTierResponse createMembershipTier(MembershipTierRequest request) {
        membershipTierRepository.lockTierLifecycle();
        String code = normalizeCode(request.getCode());
        List<MembershipTier> lockedTiers = membershipTierRepository.findAllForUpdate();
        if (lockedTiers.stream().anyMatch(tier -> tier.getCode().equals(code))) {
            throw new ConflictException(MembershipMessageConstant.TIER_INVALID_CONFIGURATION);
        }

        MembershipTier tier = MembershipTier.builder()
                .code(code)
                .displayNames(copyNames(request.getDisplayNames()))
                .descriptions(copyNames(request.getDescriptions()))
                .threshold(request.getThreshold())
                .sortOrder(request.getSortOrder() == null ? nextSortOrder(lockedTiers) : request.getSortOrder())
                .discountPercentage(request.getDiscountPercentage())
                .freeShipping(Boolean.TRUE.equals(request.getFreeShipping()))
                .benefits(copyBenefits(request.getBenefits()))
                .active(request.getActive() == null || request.getActive())
                .build();
        validateTier(tier);
        List<MembershipTier> candidateTiers = new ArrayList<>(lockedTiers);
        candidateTiers.add(tier);
        validateActiveConfiguration(candidateTiers);

        try {
            return mapToResponse(membershipTierRepository.saveAndFlush(tier));
        } catch (DataIntegrityViolationException | OptimisticLockingFailureException | OptimisticLockException exception) {
            throw new ConflictException(MembershipMessageConstant.TIER_VERSION_CONFLICT);
        }
    }

    @Override
    @Transactional
    public MembershipTierResponse updateMembershipTier(String code, MembershipTierRequest request) {
        return mapToResponse(updateTier(code, request));
    }

    @Override
    @Transactional
    public void setMembershipTierActive(String code, boolean active, Long expectedVersion) {
        membershipTierRepository.lockTierLifecycle();
        List<MembershipTier> lockedTiers = membershipTierRepository.findAllForUpdate();
        MembershipTier tier = findInLockedTiers(lockedTiers, code);
        MembershipTierRequest request = MembershipTierRequest.builder()
                .displayNames(tier.getDisplayNames())
                .descriptions(tier.getDescriptions())
                .threshold(tier.getThreshold())
                .sortOrder(tier.getSortOrder())
                .discountPercentage(tier.getDiscountPercentage())
                .freeShipping(tier.isFreeShipping())
                .benefits(tier.getBenefits())
                .active(active)
                .expectedVersion(expectedVersion)
                .build();
        updateTier(tier.getCode(), request);
    }

    @Override
    @Transactional
    public void deleteMembershipTier(String code, Long expectedVersion) {
        membershipTierRepository.lockTierLifecycle();
        List<MembershipTier> lockedTiers = membershipTierRepository.findAllForUpdate();
        MembershipTier tier = findInLockedTiers(lockedTiers, code);
        assertVersion(tier, expectedVersion);
        if (userRepository.existsByMembershipTierId(tier.getId())
                || couponRepository.existsByRequiredMembershipTierId(tier.getId())) {
            throw new ConflictException(MembershipMessageConstant.TIER_DEPENDENCY_CONFLICT);
        }
        if (tier.isActive()) {
            List<MembershipTier> remaining = lockedTiers.stream()
                    .filter(existing -> !java.util.Objects.equals(existing.getId(), tier.getId()))
                    .toList();
            validateActiveConfiguration(remaining);
        }
        try {
            membershipTierRepository.delete(tier);
            membershipTierRepository.flush();
        } catch (DataIntegrityViolationException | OptimisticLockingFailureException | OptimisticLockException exception) {
            throw new ConflictException(MembershipMessageConstant.TIER_VERSION_CONFLICT);
        }
    }

    private MembershipTier updateTier(String code, MembershipTierRequest request) {
        membershipTierRepository.lockTierLifecycle();
        List<MembershipTier> lockedTiers = membershipTierRepository.findAllForUpdate();
        MembershipTier tier = findInLockedTiers(lockedTiers, code);
        assertVersion(tier, request.getExpectedVersion());
        if (request.getCode() != null && !normalizeCode(request.getCode()).equals(tier.getCode())) {
            throw new BadRequestException(MembershipMessageConstant.TIER_CODE_INVALID);
        }
        if (request.getDisplayNames() != null) {
            tier.setDisplayNames(copyNames(request.getDisplayNames()));
        }
        if (request.getDescriptions() != null) {
            tier.setDescriptions(copyNames(request.getDescriptions()));
        }
        if (request.getThreshold() != null) {
            tier.setThreshold(request.getThreshold());
        }
        if (request.getSortOrder() != null) {
            tier.setSortOrder(request.getSortOrder());
        }
        if (request.getDiscountPercentage() != null) {
            tier.setDiscountPercentage(request.getDiscountPercentage());
        }
        if (request.getFreeShipping() != null) {
            tier.setFreeShipping(request.getFreeShipping());
        }
        if (request.getBenefits() != null) {
            tier.setBenefits(copyBenefits(request.getBenefits()));
        }
        if (request.getActive() != null) {
            tier.setActive(request.getActive());
        }

        validateTier(tier);
        validateActiveConfiguration(lockedTiers);
        try {
            return membershipTierRepository.saveAndFlush(tier);
        } catch (OptimisticLockingFailureException | OptimisticLockException exception) {
            throw new ConflictException(MembershipMessageConstant.TIER_VERSION_CONFLICT);
        }
    }

    private void validateTier(MembershipTier tier) {
        if (tier.getCode() == null || !tier.getCode().matches("[A-Z0-9_]{2,40}")) {
            throw new BadRequestException(MembershipMessageConstant.TIER_CODE_INVALID);
        }
        if (tier.getDisplayNames() == null || tier.getDisplayNames().isEmpty()
                || tier.getDisplayNames().values().stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new BadRequestException(MembershipMessageConstant.TIER_INVALID_CONFIGURATION);
        }
        if (tier.getDescriptions() == null || tier.getDescriptions().values().stream()
                .anyMatch(value -> value == null || value.isBlank())) {
            throw new BadRequestException(MembershipMessageConstant.TIER_INVALID_CONFIGURATION);
        }
        if (tier.getThreshold() == null || tier.getThreshold().signum() < 0
                || tier.getSortOrder() == null || tier.getSortOrder() < 0
                || tier.getDiscountPercentage() == null
                || tier.getDiscountPercentage().compareTo(BigDecimal.ZERO) < 0
                || tier.getDiscountPercentage().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException(MembershipMessageConstant.TIER_INVALID_CONFIGURATION);
        }
    }

    private void validateActiveConfiguration(List<MembershipTier> tiers) {
        List<MembershipTier> activeTiers = tiers.stream()
                .filter(MembershipTier::isActive)
                .sorted(Comparator.comparing(MembershipTier::getThreshold))
                .toList();
        if (activeTiers.isEmpty() || activeTiers.get(0).getThreshold().signum() != 0) {
            throw new BadRequestException(MembershipMessageConstant.TIER_INVALID_CONFIGURATION);
        }
        for (int index = 1; index < activeTiers.size(); index++) {
            MembershipTier previous = activeTiers.get(index - 1);
            MembershipTier current = activeTiers.get(index);
            if (current.getThreshold().compareTo(previous.getThreshold()) <= 0
                    || current.getSortOrder() <= previous.getSortOrder()) {
                throw new BadRequestException(MembershipMessageConstant.TIER_INVALID_CONFIGURATION);
            }
        }
    }

    private int nextSortOrder(List<MembershipTier> tiers) {
        return tiers.stream()
                .map(MembershipTier::getSortOrder)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private MembershipTier findById(Long id) {
        return membershipTierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MembershipMessageConstant.TIER_CONFIG_NOT_FOUND));
    }

    private MembershipTier findInLockedTiers(List<MembershipTier> tiers, String code) {
        String normalizedCode = normalizeCode(code);
        return tiers.stream()
                .filter(tier -> tier.getCode().equals(normalizedCode))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(MembershipMessageConstant.TIER_CONFIG_NOT_FOUND));
    }

    private void assertVersion(MembershipTier tier, Long expectedVersion) {
        if (expectedVersion == null || !expectedVersion.equals(tier.getVersion())) {
            throw new ConflictException(MembershipMessageConstant.TIER_VERSION_CONFLICT);
        }
    }

    private MembershipTierResponse mapToResponse(MembershipTier tier) {
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

    private TierConfigResponse mapToLegacyResponse(MembershipTier tier) {
        UserTier legacyTier = null;
        try {
            legacyTier = UserTier.valueOf(tier.getCode());
        } catch (IllegalArgumentException ignored) {
            // Custom tier codes have no legacy enum representation.
        }
        String description = tier.getDescriptions() == null ? null
                : tier.getDescriptions().getOrDefault("vi", tier.getDescriptions().get("en"));
        return TierConfigResponse.builder()
                .id(tier.getId())
                .tier(legacyTier)
                .code(tier.getCode())
                .threshold(tier.getThreshold())
                .description(description)
                .sortOrder(tier.getSortOrder())
                .discountPercentage(tier.getDiscountPercentage())
                .freeShipping(tier.isFreeShipping())
                .active(tier.isActive())
                .version(tier.getVersion())
                .build();
    }

    private Map<String, String> copyNames(Map<String, String> values) {
        return values == null ? new HashMap<>() : new HashMap<>(values);
    }

    private Map<String, Object> copyBenefits(Map<String, Object> values) {
        return values == null ? new HashMap<>() : new HashMap<>(values);
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}
