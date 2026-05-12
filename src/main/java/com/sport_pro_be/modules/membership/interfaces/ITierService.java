package com.sport_pro_be.modules.membership.interfaces;

import com.sport_pro_be.modules.auth.domain.User;

public interface ITierService {
    void updateUserTier(User user);
}
