package com.sport_pro_be.modules.membership.interfaces;

import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.membership.domain.MembershipTier;
import com.sport_pro_be.modules.membership.dto.MembershipProfileResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ITierService {
    void updateUserTier(User user);

    MembershipTier resolveCurrentTier(User user);

    MembershipTier getTierByCode(String code);

    boolean isTierAtLeast(User user, MembershipTier requiredTier);

    List<MembershipTier> getActiveTiers();

    MembershipProfileResponse getMembershipProfile(User user);

    void creditSpending(User user, BigDecimal amount);
}
