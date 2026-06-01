package com.sport_pro_be.modules.printing.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.printing.constant.PrintingMessageConstant;
import com.sport_pro_be.modules.printing.domain.PrintingMaterial;
import com.sport_pro_be.modules.printing.dto.PrintingDto;
import com.sport_pro_be.modules.printing.interfaces.IPrintingMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/printing/materials")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPrintingMaterialController {

    private final IPrintingMaterialService materialService;

    @GetMapping
    public ApiResponse<List<PrintingMaterial>> getAllMaterials() {
        return ApiResponse.of(null, materialService.getAllMaterials());
    }

    @PostMapping
    public ApiResponse<PrintingMaterial> createMaterial(@Valid @RequestBody PrintingDto.MaterialRequest request) {
        return ApiResponse.of(PrintingMessageConstant.CREATE_SUCCESS, materialService.createMaterial(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PrintingMaterial> updateMaterial(@PathVariable Long id, @Valid @RequestBody PrintingDto.MaterialRequest request) {
        return ApiResponse.of(PrintingMessageConstant.UPDATE_SUCCESS, materialService.updateMaterial(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ApiResponse.of(PrintingMessageConstant.DELETE_SUCCESS, null);
    }
}
