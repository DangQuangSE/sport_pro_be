package com.sport_pro_be.modules.color.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.color.constant.ColorMessageConstant;
import com.sport_pro_be.modules.color.dto.request.ColorRequest;
import com.sport_pro_be.modules.color.dto.response.ColorResponse;
import com.sport_pro_be.modules.color.interfaces.IColorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/colors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminColorController {

    private final IColorService colorService;

    @PostMapping
    public ApiResponse<ColorResponse> createColor(@Valid @RequestBody ColorRequest request) {
        return ApiResponse.of(ColorMessageConstant.COLOR_CREATED, colorService.createColor(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ColorResponse> updateColor(@PathVariable Long id, @Valid @RequestBody ColorRequest request) {
        return ApiResponse.of(ColorMessageConstant.COLOR_UPDATED, colorService.updateColor(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteColor(@PathVariable Long id) {
        colorService.deleteColor(id);
        return ApiResponse.of(ColorMessageConstant.COLOR_DELETED, null);
    }
}
