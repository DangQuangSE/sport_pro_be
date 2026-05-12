package com.sport_pro_be.modules.membership.service;

import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.membership.domain.TierConfig;
import com.sport_pro_be.modules.membership.interfaces.ITierService;
import com.sport_pro_be.modules.membership.repository.TierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TierService implements ITierService {

    private final TierRepository tierRepository;

    @Override
    @Transactional
    public void updateUserTier(User user) {
        List<TierConfig> configs = tierRepository.findAllByOrderByThresholdDesc();
        
        for (TierConfig config : configs) {
            if (user.getTotalSpending().compareTo(config.getThreshold()) >= 0) {
                user.setTier(config.getTier());
                break;
            }
        }
    }
}
