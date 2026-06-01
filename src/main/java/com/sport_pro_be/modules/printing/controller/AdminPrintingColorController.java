package com.sport_pro_be.modules.printing.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.printing.constant.PrintingMessageConstant;
import com.sport_pro_be.modules.printing.domain.PrintingColor;
import com.sport_pro_be.modules.printing.dto.PrintingDto;
import com.sport_pro_be.modules.printing.interfaces.IPrintingColorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/printing/colors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPrintingColorController {

    private final IPrintingColorService colorService;

    @GetMapping
    public ApiResponse<List<PrintingColor>> getAllColors() {
        return ApiResponse.of(null, colorService.getAllColors());
    }

    @PostMapping
    public ApiResponse<PrintingColor> createColor(@Valid @RequestBody PrintingDto.ColorRequest request) {
        return ApiResponse.of(PrintingMessageConstant.CREATE_SUCCESS, colorService.createColor(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PrintingColor> updateColor(@PathVariable Long id, @Valid @RequestBody PrintingDto.ColorRequest request) {
        return ApiResponse.of(PrintingMessageConstant.UPDATE_SUCCESS, colorService.updateColor(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteColor(@PathVariable Long id) {
        colorService.deleteColor(id);
        return ApiResponse.of(PrintingMessageConstant.DELETE_SUCCESS, null);
    }
}
