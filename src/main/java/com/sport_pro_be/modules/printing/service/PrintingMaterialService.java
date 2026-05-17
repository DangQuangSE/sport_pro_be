package com.sport_pro_be.modules.printing.service;

import com.sport_pro_be.exception.AppException;
import com.sport_pro_be.modules.printing.constant.PrintingMessageConstant;
import com.sport_pro_be.modules.printing.domain.PrintingMaterial;
import com.sport_pro_be.modules.printing.dto.PrintingDto;
import com.sport_pro_be.modules.printing.interfaces.IPrintingMaterialService;
import com.sport_pro_be.modules.printing.repository.PrintingMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrintingMaterialService implements IPrintingMaterialService {

    private final PrintingMaterialRepository materialRepository;

    @Override
    public List<PrintingMaterial> getAllMaterials() {
        return materialRepository.findAll();
    }

    @Override
    public List<PrintingMaterial> getActiveMaterials() {
        return materialRepository.findAllByIsActiveTrue();
    }

    @Override
    @Transactional
    public PrintingMaterial createMaterial(PrintingDto.MaterialRequest request) {
        PrintingMaterial material = PrintingMaterial.builder()
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .isActive(request.getIsActive())
                .build();
        return materialRepository.save(material);
    }

    @Override
    @Transactional
    public PrintingMaterial updateMaterial(Long id, PrintingDto.MaterialRequest request) {
        PrintingMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new AppException(PrintingMessageConstant.MATERIAL_NOT_FOUND, HttpStatus.NOT_FOUND));
        
        material.setName(request.getName());
        material.setDescription(request.getDescription());
        material.setBasePrice(request.getBasePrice());
        material.setIsActive(request.getIsActive());
        
        return materialRepository.save(material);
    }

    @Override
    @Transactional
    public void deleteMaterial(Long id) {
        if (!materialRepository.existsById(id)) {
            throw new AppException(PrintingMessageConstant.MATERIAL_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        materialRepository.deleteById(id);
    }
}
