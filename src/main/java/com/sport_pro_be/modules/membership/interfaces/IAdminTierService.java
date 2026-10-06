package com.sport_pro_be.modules.membership.interfaces;

import com.sport_pro_be.modules.membership.dto.TierConfigRequest;
import com.sport_pro_be.modules.membership.dto.TierConfigResponse;
import com.sport_pro_be.modules.membership.dto.MembershipTierRequest;
import com.sport_pro_be.modules.membership.dto.MembershipTierResponse;
import java.util.List;

public interface IAdminTierService {
    List<TierConfigResponse> getAllTierConfigs();
    TierConfigResponse updateTierConfig(Long id, TierConfigRequest request);
    List<MembershipTierResponse> getMembershipTiers(Boolean active);
    MembershipTierResponse createMembershipTier(MembershipTierRequest request);
    MembershipTierResponse updateMembershipTier(String code, MembershipTierRequest request);
    void setMembershipTierActive(String code, boolean active, Long expectedVersion);
    void deleteMembershipTier(String code, Long expectedVersion);
}
