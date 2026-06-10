package com.sport_pro_be.modules.size.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.size.constant.SizeMessageConstant;
import com.sport_pro_be.modules.size.dto.request.SizeGroupRequest;
import com.sport_pro_be.modules.size.dto.response.SizeGroupResponse;
import com.sport_pro_be.modules.size.interfaces.ISizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/size-groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSizeController {

    private final ISizeService sizeService;

    @PostMapping
    public ApiResponse<SizeGroupResponse> createSizeGroup(@Valid @RequestBody SizeGroupRequest request) {
        return ApiResponse.of(SizeMessageConstant.SIZE_GROUP_CREATED, sizeService.createSizeGroup(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SizeGroupResponse> updateSizeGroup(@PathVariable Long id, @Valid @RequestBody SizeGroupRequest request) {
        return ApiResponse.of(SizeMessageConstant.SIZE_GROUP_UPDATED, sizeService.updateSizeGroup(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSizeGroup(@PathVariable Long id) {
        sizeService.deleteSizeGroup(id);
        return ApiResponse.of(SizeMessageConstant.SIZE_GROUP_DELETED, null);
    }

    @GetMapping("/{id}")
    public ApiResponse<SizeGroupResponse> getSizeGroupById(@PathVariable Long id) {
        return ApiResponse.of(SizeMessageConstant.SIZE_GROUPS_RETRIEVED, sizeService.getSizeGroupById(id));
    }

    @GetMapping
    public ApiResponse<List<SizeGroupResponse>> getAllSizeGroups() {
        return ApiResponse.of(SizeMessageConstant.SIZE_GROUPS_RETRIEVED, sizeService.getAllSizeGroups());
    }
}
