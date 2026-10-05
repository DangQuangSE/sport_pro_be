package com.sport_pro_be.modules.membership.interfaces;

import com.sport_pro_be.modules.auth.domain.User;

import java.math.BigDecimal;

public interface ITierService {
    void updateUserTier(User user);

    void creditSpending(User user, BigDecimal amount);
}
