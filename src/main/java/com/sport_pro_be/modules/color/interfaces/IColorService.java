package com.sport_pro_be.modules.color.interfaces;

import com.sport_pro_be.modules.color.dto.request.ColorRequest;
import com.sport_pro_be.modules.color.dto.response.ColorResponse;

import java.util.List;

public interface IColorService {
    ColorResponse createColor(ColorRequest request);
    ColorResponse updateColor(Long id, ColorRequest request);
    void deleteColor(Long id);
    ColorResponse getColorById(Long id);
    List<ColorResponse> getAllColors();
}
