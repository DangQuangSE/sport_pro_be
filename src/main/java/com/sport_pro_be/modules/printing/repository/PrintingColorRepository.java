package com.sport_pro_be.modules.printing.repository;

import com.sport_pro_be.modules.printing.domain.PrintingColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrintingColorRepository extends JpaRepository<PrintingColor, Long> {
    List<PrintingColor> findAllByIsActiveTrue();
}
