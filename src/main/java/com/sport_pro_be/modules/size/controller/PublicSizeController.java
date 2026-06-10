package com.sport_pro_be.modules.size.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.size.constant.SizeMessageConstant;
import com.sport_pro_be.modules.size.dto.response.SizeGroupResponse;
import com.sport_pro_be.modules.size.interfaces.ISizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/size-groups")
@RequiredArgsConstructor
public class PublicSizeController {

    private final ISizeService sizeService;

    @GetMapping
    public ApiResponse<List<SizeGroupResponse>> getAllSizeGroups() {
        return ApiResponse.of(SizeMessageConstant.SIZE_GROUPS_RETRIEVED, sizeService.getAllSizeGroups());
    }

    @GetMapping("/{id}")
    public ApiResponse<SizeGroupResponse> getSizeGroupById(@PathVariable Long id) {
        return ApiResponse.of(SizeMessageConstant.SIZE_GROUPS_RETRIEVED, sizeService.getSizeGroupById(id));
    }
}
