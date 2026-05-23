package com.sport_pro_be.modules.color.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.color.constant.ColorMessageConstant;
import com.sport_pro_be.modules.color.dto.response.ColorResponse;
import com.sport_pro_be.modules.color.interfaces.IColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/colors")
@RequiredArgsConstructor
public class PublicColorController {

    private final IColorService colorService;

    @GetMapping
    public ApiResponse<List<ColorResponse>> getAllColors() {
        return ApiResponse.of(ColorMessageConstant.COLORS_RETRIEVED, colorService.getAllColors());
    }

    @GetMapping("/{id}")
    public ApiResponse<ColorResponse> getColorById(@PathVariable Long id) {
        return ApiResponse.of(ColorMessageConstant.COLORS_RETRIEVED, colorService.getColorById(id));
    }
}
