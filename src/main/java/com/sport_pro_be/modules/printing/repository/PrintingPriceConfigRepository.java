package com.sport_pro_be.modules.printing.repository;

import com.sport_pro_be.modules.printing.domain.PrintingPriceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrintingPriceConfigRepository extends JpaRepository<PrintingPriceConfig, Long> {
}
