package com.sport_pro_be.modules.size.service;

import com.sport_pro_be.exception.ConflictException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.product.repository.ProductRepository;
import com.sport_pro_be.modules.size.constant.SizeMessageConstant;
import com.sport_pro_be.modules.size.domain.SizeGroup;
import com.sport_pro_be.modules.size.domain.SizeOption;
import com.sport_pro_be.modules.size.dto.request.SizeGroupRequest;
import com.sport_pro_be.modules.size.dto.request.SizeOptionRequest;
import com.sport_pro_be.modules.size.dto.response.SizeGroupResponse;
import com.sport_pro_be.modules.size.dto.response.SizeOptionResponse;
import com.sport_pro_be.modules.size.interfaces.ISizeService;
import com.sport_pro_be.modules.size.repository.SizeGroupRepository;
import com.sport_pro_be.modules.size.repository.SizeOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SizeService implements ISizeService {

    private final SizeGroupRepository sizeGroupRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product_details", "size_groups" }, allEntries = true)
    public SizeGroupResponse createSizeGroup(SizeGroupRequest request) {
        if (sizeGroupRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException(SizeMessageConstant.SIZE_GROUP_NAME_EXISTS);
        }

        SizeGroup sizeGroup = SizeGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        if (request.getSizes() != null) {
            for (SizeOptionRequest sor : request.getSizes()) {
                SizeOption sizeOption = SizeOption.builder()
                        .name(sor.getName())
                        .displayOrder(sor.getDisplayOrder() != null ? sor.getDisplayOrder() : 0)
                        .sizeGroup(sizeGroup)
                        .build();
                sizeGroup.getSizes().add(sizeOption);
            }
        }

        sizeGroup = sizeGroupRepository.save(sizeGroup);
        return mapToResponse(sizeGroup);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product_details", "size_groups" }, allEntries = true)
    public SizeGroupResponse updateSizeGroup(Long id, SizeGroupRequest request) {
        SizeGroup sizeGroup = sizeGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SizeMessageConstant.SIZE_GROUP_NOT_FOUND));

        if (sizeGroupRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new ConflictException(SizeMessageConstant.SIZE_GROUP_NAME_EXISTS);
        }

        sizeGroup.setName(request.getName());
        sizeGroup.setDescription(request.getDescription());

        // Remove old sizes and replace with new ones
        sizeGroup.getSizes().clear();
        if (request.getSizes() != null) {
            for (SizeOptionRequest sor : request.getSizes()) {
                SizeOption sizeOption = SizeOption.builder()
                        .name(sor.getName())
                        .displayOrder(sor.getDisplayOrder() != null ? sor.getDisplayOrder() : 0)
                        .sizeGroup(sizeGroup)
                        .build();
                sizeGroup.getSizes().add(sizeOption);
            }
        }

        sizeGroup = sizeGroupRepository.save(sizeGroup);
        return mapToResponse(sizeGroup);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product_details", "size_groups" }, allEntries = true)
    public void deleteSizeGroup(Long id) {
        SizeGroup sizeGroup = sizeGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SizeMessageConstant.SIZE_GROUP_NOT_FOUND));

        if (productRepository.existsBySizeGroupId(id)) {
            throw new ConflictException(SizeMessageConstant.SIZE_GROUP_IN_USE);
        }

        sizeGroupRepository.delete(sizeGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public SizeGroupResponse getSizeGroupById(Long id) {
        SizeGroup sizeGroup = sizeGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SizeMessageConstant.SIZE_GROUP_NOT_FOUND));
        return mapToResponse(sizeGroup);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "size_groups")
    public List<SizeGroupResponse> getAllSizeGroups() {
        return sizeGroupRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SizeGroupResponse mapToResponse(SizeGroup sg) {
        List<SizeOptionResponse> sizes = new ArrayList<>();
        if (sg.getSizes() != null) {
            sizes = sg.getSizes().stream()
                    .map(so -> SizeOptionResponse.builder()
                            .id(so.getId())
                            .name(so.getName())
                            .displayOrder(so.getDisplayOrder())
                            .build())
                    .collect(Collectors.toList());
        }

        return SizeGroupResponse.builder()
                .id(sg.getId())
                .name(sg.getName())
                .description(sg.getDescription())
                .sizes(sizes)
                .build();
    }
}
