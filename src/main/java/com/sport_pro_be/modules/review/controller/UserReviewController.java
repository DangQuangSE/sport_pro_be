package com.sport_pro_be.modules.review.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.review.constant.ReviewMessageConstant;
import com.sport_pro_be.modules.review.dto.ReviewRequest;
import com.sport_pro_be.modules.review.dto.ReviewResponse;
import com.sport_pro_be.modules.review.interfaces.IReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal User user,
            @Valid @RequestPart("review") ReviewRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ApiResponse.of(ReviewMessageConstant.CREATE_SUCCESS, 
                reviewService.createReview(user.getId(), request, images));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ApiResponse<ReviewResponse> updateReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestPart("review") ReviewRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ApiResponse.of(ReviewMessageConstant.UPDATE_SUCCESS, 
                reviewService.updateReview(user.getId(), id, request, images));
    }
}
