package com.sport_pro_be.modules.membership.service;

import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.membership.domain.TierConfig;
import com.sport_pro_be.modules.membership.interfaces.ITierService;
import com.sport_pro_be.modules.membership.repository.TierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Override
    @Transactional
    public void creditSpending(User user, BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Spending amount must be non-negative");
        }

        BigDecimal currentSpending = user.getTotalSpending() == null
                ? BigDecimal.ZERO
                : user.getTotalSpending();
        user.setTotalSpending(currentSpending.add(amount));
        updateUserTier(user);
    }
}
