package com.sport_pro_be.modules.review.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.review.constant.ReviewMessageConstant;
import com.sport_pro_be.modules.review.dto.ReviewResponse;
import com.sport_pro_be.modules.review.interfaces.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final IReviewService reviewService;

    @GetMapping
    public ApiResponse<Page<ReviewResponse>> getAllReviews(Pageable pageable) {
        return ApiResponse.of(null, reviewService.getAllReviews(pageable));
    }

    @PostMapping("/{id}/reply")
    public ApiResponse<ReviewResponse> replyReview(
            @PathVariable Long id,
            @RequestBody String replyComment) {
        return ApiResponse.of(ReviewMessageConstant.UPDATE_SUCCESS, reviewService.replyReview(id, replyComment));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ApiResponse.of(ReviewMessageConstant.DELETE_SUCCESS, null);
    }
}
