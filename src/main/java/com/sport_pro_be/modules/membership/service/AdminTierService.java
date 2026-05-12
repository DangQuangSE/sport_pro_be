package com.sport_pro_be.modules.membership.service;

import com.sport_pro_be.modules.membership.constant.MembershipMessageConstant;
import com.sport_pro_be.modules.membership.domain.TierConfig;
import com.sport_pro_be.modules.membership.dto.TierConfigRequest;
import com.sport_pro_be.modules.membership.dto.TierConfigResponse;
import com.sport_pro_be.modules.membership.interfaces.IAdminTierService;
import com.sport_pro_be.modules.membership.repository.TierRepository;
import com.sport_pro_be.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTierService implements IAdminTierService {

    private final TierRepository tierRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TierConfigResponse> getAllTierConfigs() {
        return tierRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TierConfigResponse updateTierConfig(Long id, TierConfigRequest request) {
        TierConfig config = tierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MembershipMessageConstant.TIER_CONFIG_NOT_FOUND));
        
        config.setThreshold(request.getThreshold());
        config.setDescription(request.getDescription());
        
        config = tierRepository.save(config);
        return mapToResponse(config);
    }

    private TierConfigResponse mapToResponse(TierConfig config) {
        return TierConfigResponse.builder()
                .id(config.getId())
                .tier(config.getTier())
                .threshold(config.getThreshold())
                .description(config.getDescription())
                .build();
    }
}
