package com.sport_pro_be.product.service;

import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.product.constant.ProductMessageConstant;
import com.sport_pro_be.product.domain.Product;
import com.sport_pro_be.product.domain.ProductImage;
import com.sport_pro_be.product.domain.ProductVariant;
import com.sport_pro_be.product.dto.request.ProductImageRequest;
import com.sport_pro_be.product.dto.response.ProductImageResponse;
import com.sport_pro_be.product.interfaces.IProductImageService;
import com.sport_pro_be.product.repository.ProductImageRepository;
import com.sport_pro_be.product.repository.ProductRepository;
import com.sport_pro_be.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductImageService implements IProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public ProductImageResponse addImage(Long productId, ProductImageRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ProductMessageConstant.PRODUCT_NOT_FOUND));

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(ProductMessageConstant.VARIANT_NOT_FOUND));
        }

        ProductImage productImage = ProductImage.builder()
                .product(product)
                .variant(variant)
                .imageUrl(request.getImageUrl())
                .isThumbnail(request.getIsThumbnail())
                .sortOrder(request.getSortOrder())
                .build();

        productImage = productImageRepository.save(productImage);

        return ProductImageResponse.builder()
                .id(productImage.getId())
                .imageUrl(productImage.getImageUrl())
                .isThumbnail(productImage.getIsThumbnail())
                .sortOrder(productImage.getSortOrder())
                .build();
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(ProductMessageConstant.IMAGE_NOT_FOUND));
        productImageRepository.delete(productImage);
    }
}

