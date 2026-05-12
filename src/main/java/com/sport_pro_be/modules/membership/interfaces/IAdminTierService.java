package com.sport_pro_be.modules.membership.interfaces;

import com.sport_pro_be.modules.membership.dto.TierConfigRequest;
import com.sport_pro_be.modules.membership.dto.TierConfigResponse;
import java.util.List;

public interface IAdminTierService {
    List<TierConfigResponse> getAllTierConfigs();
    TierConfigResponse updateTierConfig(Long id, TierConfigRequest request);
}
