package com.sport_pro_be.modules.size.repository;

import com.sport_pro_be.modules.size.domain.SizeOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeOptionRepository extends JpaRepository<SizeOption, Long> {
    List<SizeOption> findBySizeGroupIdOrderByDisplayOrderAsc(Long sizeGroupId);
    void deleteBySizeGroupId(Long sizeGroupId);
}
