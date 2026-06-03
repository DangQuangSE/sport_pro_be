package com.sport_pro_be.modules.review.interfaces;

import com.sport_pro_be.modules.review.dto.ReviewRequest;
import com.sport_pro_be.modules.review.dto.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IReviewService {
    ReviewResponse createReview(Long userId, ReviewRequest request, List<MultipartFile> images);
    ReviewResponse updateReview(Long userId, Long reviewId, ReviewRequest request, List<MultipartFile> images);
    Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable);
    void deleteReview(Long reviewId);
    ReviewResponse replyReview(Long reviewId, String replyComment);
    Page<ReviewResponse> getAllReviews(Pageable pageable);
}
