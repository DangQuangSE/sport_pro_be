package com.sport_pro_be.modules.size.repository;

import com.sport_pro_be.modules.size.domain.SizeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SizeGroupRepository extends JpaRepository<SizeGroup, Long> {
    Optional<SizeGroup> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
