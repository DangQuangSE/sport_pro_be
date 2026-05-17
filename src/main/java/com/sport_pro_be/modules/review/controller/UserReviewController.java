package com.sport_pro_be.modules.review.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.common.SecurityUtils;
import com.sport_pro_be.modules.review.constant.ReviewMessageConstant;
import com.sport_pro_be.modules.review.dto.ReviewRequest;
import com.sport_pro_be.modules.review.dto.ReviewResponse;
import com.sport_pro_be.modules.review.interfaces.IReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user/reviews")
@RequiredArgsConstructor
public class UserReviewController {

    private final IReviewService reviewService;

    @PostMapping(consumes = "multipart/form-data")
    public ApiResponse<ReviewResponse> createReview(
            @Valid @RequestPart("review") ReviewRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.of(ReviewMessageConstant.CREATE_SUCCESS, 
                reviewService.createReview(userId, request, images));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestPart("review") ReviewRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.of(ReviewMessageConstant.UPDATE_SUCCESS, 
                reviewService.updateReview(userId, id, request, images));
    }
}
