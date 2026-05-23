package com.sport_pro_be.modules.color.service;

import com.sport_pro_be.exception.ConflictException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.color.constant.ColorMessageConstant;
import com.sport_pro_be.modules.color.domain.Color;
import com.sport_pro_be.modules.color.dto.request.ColorRequest;
import com.sport_pro_be.modules.color.dto.response.ColorResponse;
import com.sport_pro_be.modules.color.interfaces.IColorService;
import com.sport_pro_be.modules.color.repository.ColorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColorService implements IColorService {

    private final ColorRepository colorRepository;

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product_details", "colors" }, allEntries = true)
    public ColorResponse createColor(ColorRequest request) {
        if (colorRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException(ColorMessageConstant.COLOR_NAME_EXISTS);
        }

        Color color = Color.builder()
                .name(request.getName())
                .hexCode(request.getHexCode())
                .build();

        color = colorRepository.save(color);
        return mapToResponse(color);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product_details", "colors" }, allEntries = true)
    public ColorResponse updateColor(Long id, ColorRequest request) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ColorMessageConstant.COLOR_NOT_FOUND));

        if (colorRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new ConflictException(ColorMessageConstant.COLOR_NAME_EXISTS);
        }

        color.setName(request.getName());
        color.setHexCode(request.getHexCode());

        color = colorRepository.save(color);
        return mapToResponse(color);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product_details", "colors" }, allEntries = true)
    public void deleteColor(Long id) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ColorMessageConstant.COLOR_NOT_FOUND));
        colorRepository.delete(color);
    }

    @Override
    @Transactional(readOnly = true)
    public ColorResponse getColorById(Long id) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ColorMessageConstant.COLOR_NOT_FOUND));
        return mapToResponse(color);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "colors")
    public List<ColorResponse> getAllColors() {
        return colorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ColorResponse mapToResponse(Color color) {
        return ColorResponse.builder()
                .id(color.getId())
                .name(color.getName())
                .hexCode(color.getHexCode())
                .build();
    }
}
