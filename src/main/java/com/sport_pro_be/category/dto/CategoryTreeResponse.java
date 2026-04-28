package com.sport_pro_be.category.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryTreeResponse {
    private Long id;
    private String name;
    private String slug;
    private String imageUrl;
    private Integer displayOrder;
    private List<CategoryTreeResponse> children;
}
