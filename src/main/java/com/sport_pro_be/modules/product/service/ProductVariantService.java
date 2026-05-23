package com.sport_pro_be.modules.product.service;

import com.sport_pro_be.exception.ConflictException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.product.constant.ProductMessageConstant;
import com.sport_pro_be.modules.product.domain.Product;
import com.sport_pro_be.modules.product.domain.ProductVariant;
import com.sport_pro_be.modules.product.dto.request.ProductVariantRequest;
import com.sport_pro_be.modules.product.dto.response.ProductVariantResponse;
import com.sport_pro_be.modules.product.interfaces.IProductVariantService;
import com.sport_pro_be.modules.product.repository.ProductRepository;
import com.sport_pro_be.modules.product.repository.ProductVariantRepository;
import com.sport_pro_be.modules.color.constant.ColorMessageConstant;
import com.sport_pro_be.modules.color.domain.Color;
import com.sport_pro_be.modules.color.repository.ColorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductVariantService implements IProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product_details" }, allEntries = true)
    public ProductVariantResponse createVariant(Long productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ProductMessageConstant.PRODUCT_NOT_FOUND));

        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new ConflictException(ProductMessageConstant.SKU_ALREADY_EXISTS);
        }

        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new ResourceNotFoundException(ColorMessageConstant.COLOR_NOT_FOUND));

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(request.getSku())
                .size(request.getSize())
                .color(color)
                .originalPrice(request.getOriginalPrice())
                .salePrice(request.getSalePrice())
                .stockQuantity(request.getStockQuantity())
                .status(request.getStatus())
                .build();

        variant = productVariantRepository.save(variant);
        return mapToResponse(variant);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product_details" }, allEntries = true)
    public ProductVariantResponse updateVariant(Long variantId, ProductVariantRequest request) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(ProductMessageConstant.VARIANT_NOT_FOUND));

        if (productVariantRepository.existsBySkuAndIdNot(request.getSku(), variantId)) {
            throw new ConflictException(ProductMessageConstant.SKU_ALREADY_EXISTS);
        }

        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new ResourceNotFoundException(ColorMessageConstant.COLOR_NOT_FOUND));

        variant.setSku(request.getSku());
        variant.setSize(request.getSize());
        variant.setColor(color);
        variant.setOriginalPrice(request.getOriginalPrice());
        variant.setSalePrice(request.getSalePrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setStatus(request.getStatus());

        variant = productVariantRepository.save(variant);
        return mapToResponse(variant);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "products", "product_details" }, allEntries = true)
    public void deleteVariant(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(ProductMessageConstant.VARIANT_NOT_FOUND));
        productVariantRepository.delete(variant);
    }

    private ProductVariantResponse mapToResponse(ProductVariant v) {
        return ProductVariantResponse.builder()
                .id(v.getId())
                .sku(v.getSku())
                .size(v.getSize())
                .colorId(v.getColor() != null ? v.getColor().getId() : null)
                .colorName(v.getColor() != null ? v.getColor().getName() : v.getColorOld())
                .colorHex(v.getColor() != null ? v.getColor().getHexCode() : "#000000")
                .originalPrice(v.getOriginalPrice())
                .salePrice(v.getSalePrice())
                .stockQuantity(v.getStockQuantity())
                .status(v.getStatus())
                .build();
    }
}


