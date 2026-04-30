package com.sport_pro_be.product.service;

import com.sport_pro_be.exception.ConflictException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.product.domain.Product;
import com.sport_pro_be.product.domain.ProductVariant;
import com.sport_pro_be.product.dto.request.ProductVariantRequest;
import com.sport_pro_be.product.dto.response.ProductVariantResponse;
import com.sport_pro_be.product.interfaces.IProductVariantService;
import com.sport_pro_be.product.repository.ProductRepository;
import com.sport_pro_be.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductVariantService implements IProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductVariantResponse createVariant(Long productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new ConflictException("SKU already exists");
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(request.getSku())
                .size(request.getSize())
                .color(request.getColor())
                .price(request.getPrice())
                .salePrice(request.getSalePrice())
                .stockQuantity(request.getStockQuantity())
                .status(request.getStatus())
                .build();

        variant = productVariantRepository.save(variant);
        return mapToResponse(variant);
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariant(Long variantId, ProductVariantRequest request) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant not found"));

        if (productVariantRepository.existsBySkuAndIdNot(request.getSku(), variantId)) {
            throw new ConflictException("SKU already exists");
        }

        variant.setSku(request.getSku());
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setPrice(request.getPrice());
        variant.setSalePrice(request.getSalePrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setStatus(request.getStatus());

        variant = productVariantRepository.save(variant);
        return mapToResponse(variant);
    }

    @Override
    @Transactional
    public void deleteVariant(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant not found"));
        productVariantRepository.delete(variant);
    }

    private ProductVariantResponse mapToResponse(ProductVariant v) {
        return ProductVariantResponse.builder()
                .id(v.getId())
                .sku(v.getSku())
                .size(v.getSize())
                .color(v.getColor())
                .price(v.getPrice())
                .salePrice(v.getSalePrice())
                .stockQuantity(v.getStockQuantity())
                .status(v.getStatus())
                .build();
    }
}
