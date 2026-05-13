package com.sport_pro_be.modules.custom_design.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.custom_design.constant.CustomDesignMessageConstant;
import com.sport_pro_be.modules.custom_design.dto.CustomDesignRequest;
import com.sport_pro_be.modules.custom_design.dto.CustomDesignResponse;
import com.sport_pro_be.modules.custom_design.interfaces.ICustomDesignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/custom-designs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CustomDesignController {

    private final ICustomDesignService customDesignService;

    /**
     * POST /api/custom-designs
     * Saves a new custom design. Accepts multipart form data (image file + JSON parts).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CustomDesignResponse>> saveDesign(
            Authentication authentication,
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("data") CustomDesignRequest request) {
        Long userId = extractUserId(authentication);
        CustomDesignResponse response = customDesignService.saveDesign(userId, file, request);
        return ResponseEntity.ok(ApiResponse.of(CustomDesignMessageConstant.DESIGN_SAVED_SUCCESS, response));
    }

    /**
     * GET /api/custom-designs
     * Returns a paginated list of the authenticated user's designs.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomDesignResponse>>> getMyDesigns(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = extractUserId(authentication);
        Page<CustomDesignResponse> response = customDesignService.getMyDesigns(userId, pageable);
        return ResponseEntity.ok(ApiResponse.of(null, response));
    }

    /**
     * GET /api/custom-designs/{id}
     * Returns details of a specific design. Returns 404 if not found or does not belong to the user.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomDesignResponse>> getDesignDetail(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = extractUserId(authentication);
        CustomDesignResponse response = customDesignService.getDesignDetail(userId, id);
        return ResponseEntity.ok(ApiResponse.of(null, response));
    }

    private Long extractUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
