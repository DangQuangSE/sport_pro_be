package com.sport_pro_be.modules.product.service;

import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.product.constant.ProductMessageConstant;
import com.sport_pro_be.modules.product.domain.Product;
import com.sport_pro_be.modules.product.domain.ProductImage;
import com.sport_pro_be.modules.product.domain.ProductVariant;
import com.sport_pro_be.modules.product.dto.response.ProductImageResponse;
import com.sport_pro_be.modules.product.interfaces.IProductImageService;
import com.sport_pro_be.modules.product.repository.ProductImageRepository;
import com.sport_pro_be.modules.product.repository.ProductRepository;
import com.sport_pro_be.modules.product.repository.ProductVariantRepository;
import com.sport_pro_be.modules.upload.interfaces.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductImageService implements IProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final IUploadService uploadService;

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product_details" }, allEntries = true)
    public ProductImageResponse addImage(Long productId, MultipartFile file, Long variantId, Boolean isThumbnail, Integer sortOrder) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ProductMessageConstant.PRODUCT_NOT_FOUND));

        ProductVariant variant = null;
        if (variantId != null) {
            variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new ResourceNotFoundException(ProductMessageConstant.VARIANT_NOT_FOUND));
        }

        // Upload to Cloudinary
        String imageUrl = uploadService.uploadFile(file, "products");

        ProductImage productImage = ProductImage.builder()
                .product(product)
                .variant(variant)
                .imageUrl(imageUrl)
                .isThumbnail(isThumbnail != null ? isThumbnail : false)
                .sortOrder(sortOrder != null ? sortOrder : 0)
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
    @CacheEvict(value = { "products", "product_details" }, allEntries = true)
    public void deleteImage(Long imageId) {
        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(ProductMessageConstant.IMAGE_NOT_FOUND));
        
        // Delete from Cloudinary
        uploadService.deleteFile(productImage.getImageUrl());
        
        productImageRepository.delete(productImage);
    }
}


