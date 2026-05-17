package com.sport_pro_be.modules.printing.interfaces;

import com.sport_pro_be.modules.printing.domain.PrintingMaterial;
import com.sport_pro_be.modules.printing.dto.PrintingDto;

import java.util.List;

public interface IPrintingMaterialService {
    List<PrintingMaterial> getAllMaterials();
    List<PrintingMaterial> getActiveMaterials();
    PrintingMaterial createMaterial(PrintingDto.MaterialRequest request);
    PrintingMaterial updateMaterial(Long id, PrintingDto.MaterialRequest request);
    void deleteMaterial(Long id);
}
