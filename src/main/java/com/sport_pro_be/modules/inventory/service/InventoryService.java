package com.sport_pro_be.modules.inventory.service;

import com.sport_pro_be.exception.BadRequestException;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.inventory.constant.InventoryMessageConstant;
import com.sport_pro_be.modules.inventory.dto.response.LowStockResponse;
import com.sport_pro_be.modules.inventory.interfaces.IInventoryService;
import com.sport_pro_be.modules.product.domain.ProductVariant;
import com.sport_pro_be.modules.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService implements IInventoryService {

    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public void deductStock(Long variantId, int quantity) {
        ProductVariant variant = findVariantOrThrow(variantId);

        if (variant.getStockQuantity() < quantity) {
            throw new BadRequestException(
                    String.format(InventoryMessageConstant.INSUFFICIENT_STOCK,
                            variant.getSku(), quantity, variant.getStockQuantity())
            );
        }

        try {
            variant.setStockQuantity(variant.getStockQuantity() - quantity);
            productVariantRepository.save(variant);
            log.info("Stock deducted: variantId={}, quantity={}, remaining={}",
                    variantId, quantity, variant.getStockQuantity());
        } catch (ObjectOptimisticLockingFailureException ex) {
            log.warn("Optimistic lock conflict on deductStock: variantId={}", variantId);
            throw new BadRequestException(InventoryMessageConstant.STOCK_CONFLICT);
        }
    }

    @Override
    @Transactional
    public void restoreStock(Long variantId, int quantity) {
        ProductVariant variant = findVariantOrThrow(variantId);
        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        productVariantRepository.save(variant);
        log.info("Stock restored: variantId={}, quantity={}, newTotal={}",
                variantId, quantity, variant.getStockQuantity());
    }

    @Override
    @Transactional
    public LowStockResponse restock(Long variantId, int quantity) {
        ProductVariant variant = findVariantOrThrow(variantId);
        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        productVariantRepository.save(variant);
        log.info("Stock restocked: variantId={}, addedQuantity={}, newTotal={}",
                variantId, quantity, variant.getStockQuantity());
        return mapToLowStockResponse(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockResponse> getLowStockVariants() {
        return productVariantRepository.findLowStockVariants()
                .stream()
                .map(this::mapToLowStockResponse)
                .toList();
    }

    private ProductVariant findVariantOrThrow(Long variantId) {
        return productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(InventoryMessageConstant.VARIANT_NOT_FOUND));
    }

    private LowStockResponse mapToLowStockResponse(ProductVariant variant) {
        return LowStockResponse.builder()
                .variantId(variant.getId())
                .sku(variant.getSku())
                .productName(variant.getProduct().getName())
                .size(variant.getSize())
                .color(variant.getColor())
                .stockQuantity(variant.getStockQuantity())
                .lowStockThreshold(variant.getLowStockThreshold())
                .build();
    }
}
