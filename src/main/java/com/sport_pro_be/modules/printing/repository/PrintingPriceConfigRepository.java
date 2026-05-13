package com.sport_pro_be.modules.printing.repository;

import com.sport_pro_be.modules.printing.domain.PrintingPriceConfig;
import com.sport_pro_be.modules.printing.enums.PrintingElementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrintingPriceConfigRepository extends JpaRepository<PrintingPriceConfig, Long> {

    Optional<PrintingPriceConfig> findByType(PrintingElementType type);
}
