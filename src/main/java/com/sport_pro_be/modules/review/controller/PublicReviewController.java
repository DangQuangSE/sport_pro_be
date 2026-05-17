package com.sport_pro_be.modules.review.controller;

import com.sport_pro_be.common.ApiResponse;
import com.sport_pro_be.modules.review.dto.ReviewResponse;
import com.sport_pro_be.modules.review.interfaces.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/reviews")
@RequiredArgsConstructor
public class PublicReviewController {

    private final IReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ApiResponse<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            Pageable pageable) {
        return ApiResponse.of(null, reviewService.getProductReviews(productId, pageable));
    }
}
