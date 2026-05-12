package com.sport_pro_be.modules.printing.repository;

import com.sport_pro_be.modules.printing.domain.PrintingMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrintingMaterialRepository extends JpaRepository<PrintingMaterial, Long> {
    List<PrintingMaterial> findAllByIsActiveTrue();
}
