package com.sport_pro_be.modules.user_address.repository;

import com.sport_pro_be.modules.user_address.domain.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findAllByUserIdOrderByIsDefaultDesc(Long userId);

    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Modifying
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.user.id = :userId")
    void resetDefaultByUserId(Long userId);
}
