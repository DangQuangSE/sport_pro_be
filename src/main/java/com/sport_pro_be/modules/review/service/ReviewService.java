package com.sport_pro_be.modules.review.service;

import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import com.sport_pro_be.modules.order.domain.OrderItem;
import com.sport_pro_be.modules.order.enums.OrderStatus;
import com.sport_pro_be.modules.order.repository.OrderItemRepository;
import com.sport_pro_be.modules.product.domain.Product;
import com.sport_pro_be.modules.product.repository.ProductRepository;
import com.sport_pro_be.modules.review.constant.ReviewMessageConstant;
import com.sport_pro_be.modules.review.domain.ProductReview;
import com.sport_pro_be.modules.review.dto.ReviewRequest;
import com.sport_pro_be.modules.review.dto.ReviewResponse;
import com.sport_pro_be.modules.review.interfaces.IReviewService;
import com.sport_pro_be.modules.review.repository.ProductReviewRepository;
import com.sport_pro_be.modules.upload.interfaces.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final IUploadService uploadService;

    @Override
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request, List<MultipartFile> imageFiles) {
        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new ResourceNotFoundException(ReviewMessageConstant.ORDER_ITEM_NOT_FOUND));

        if (!orderItem.getOrder().getUser().getId().equals(userId)) {
            throw new BadRequestException(ReviewMessageConstant.UNAUTHORIZED_EDIT);
        }

        if (orderItem.getOrder().getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException(ReviewMessageConstant.ORDER_NOT_DELIVERED);
        }

        if (reviewRepository.existsByOrderItemId(request.getOrderItemId())) {
            throw new BadRequestException(ReviewMessageConstant.ALREADY_REVIEWED);
        }

        User user = userRepository.findById(userId).orElseThrow();
        Product product = orderItem.getProductVariant().getProduct();

        List<String> imageUrls = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            imageFiles.forEach(file -> imageUrls.add(uploadService.uploadFile(file, "reviews")));
        }

        ProductReview review = ProductReview.builder()
                .user(user)
                .product(product)
                .orderItem(orderItem)
                .rating(request.getRating())
                .comment(request.getComment())
                .images(imageUrls)
                .build();

        review = reviewRepository.save(review);
        updateProductRatingSummary(product);

        return mapToResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, ReviewRequest request, List<MultipartFile> imageFiles) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(ReviewMessageConstant.NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new BadRequestException(ReviewMessageConstant.UNAUTHORIZED_EDIT);
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            imageFiles.forEach(file -> imageUrls.add(uploadService.uploadFile(file, "reviews")));
            review.setImages(imageUrls);
        }

        review = reviewRepository.save(review);
        updateProductRatingSummary(review.getProduct());

        return mapToResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndIsActiveTrue(productId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(ReviewMessageConstant.NOT_FOUND));
        Product product = review.getProduct();
        reviewRepository.delete(review);
        updateProductRatingSummary(product);
    }

    @Override
    @Transactional
    public ReviewResponse replyReview(Long reviewId, String replyComment) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(ReviewMessageConstant.NOT_FOUND));
        review.setReplyComment(replyComment);
        return mapToResponse(reviewRepository.save(review));
    }

    private void updateProductRatingSummary(Product product) {
        List<ProductReview> reviews = reviewRepository.findByProductIdAndIsActiveTrue(product.getId());
        int count = reviews.size();
        double average = 0.0;
        if (count > 0) {
            average = reviews.stream()
                    .mapToInt(ProductReview::getRating)
                    .average()
                    .orElse(0.0);
        }
        product.setReviewCount(count);
        product.setAverageRating(average);
        productRepository.save(product);
    }

    private ReviewResponse mapToResponse(ProductReview review) {
        String name = "Guest";
        if (review.getUser().getFirstName() != null && review.getUser().getLastName() != null) {
            name = review.getUser().getFirstName() + " " + review.getUser().getLastName();
        } else if (review.getUser().getEmail() != null) {
            name = review.getUser().getEmail();
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .userName(name)
                .userAvatar(review.getUser().getAvatar())
                .rating(review.getRating())
                .comment(review.getComment())
                .images(review.getImages())
                .replyComment(review.getReplyComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
