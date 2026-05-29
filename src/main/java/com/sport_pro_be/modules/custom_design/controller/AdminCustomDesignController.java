package com.sport_pro_be.modules.custom_design.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.custom_design.dto.CustomDesignResponse;
import com.sport_pro_be.modules.custom_design.interfaces.ICustomDesignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/custom-designs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomDesignController {

    private final ICustomDesignService customDesignService;

    /**
     * GET /api/admin/custom-designs/{id}
     * Returns custom design details for administration and printing support.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomDesignResponse>> getDesignDetailAdmin(@PathVariable Long id) {
        CustomDesignResponse response = customDesignService.getDesignDetailAdmin(id);
        return ResponseEntity.ok(ApiResponse.of(null, response));
    }
}
