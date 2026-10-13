package com.sport_pro_be.modules.membership.repository;

import com.sport_pro_be.modules.membership.domain.MembershipTier;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {

    Optional<MembershipTier> findByCode(String code);

    boolean existsByCode(String code);

    List<MembershipTier> findAllByOrderBySortOrderAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tier FROM MembershipTier tier ORDER BY tier.sortOrder ASC")
    List<MembershipTier> findAllForUpdate();

    /**
     * Serialize tier lifecycle mutations, including inserts where no row-level
     * lock exists yet for the candidate tier.
     */
    @Modifying
    @Query(value = "LOCK TABLE membership_tiers IN SHARE ROW EXCLUSIVE MODE", nativeQuery = true)
    void lockTierLifecycle();

    List<MembershipTier> findAllByActiveTrueOrderBySortOrderAsc();

    Optional<MembershipTier> findTopByActiveTrueAndThresholdLessThanEqualOrderByThresholdDesc(BigDecimal threshold);

    boolean existsByThresholdAndActiveTrue(BigDecimal threshold);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByIdAndActiveTrue(Long id);
}
