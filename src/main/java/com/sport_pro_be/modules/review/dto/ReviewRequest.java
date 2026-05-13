package com.sport_pro_be.modules.review.dto;

import com.sport_pro_be.modules.review.constant.ReviewMessageConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull(message = ReviewMessageConstant.RATING_REQUIRED)
    @Min(value = 1, message = ReviewMessageConstant.RATING_RANGE)
    @Max(value = 5, message = ReviewMessageConstant.RATING_RANGE)
    private Integer rating;

    @NotBlank(message = ReviewMessageConstant.COMMENT_REQUIRED)
    private String comment;

    @NotNull(message = ReviewMessageConstant.ORDER_ITEM_NOT_FOUND)
    private Long orderItemId;
}
