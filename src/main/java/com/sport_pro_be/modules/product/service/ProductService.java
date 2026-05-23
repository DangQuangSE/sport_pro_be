package com.sport_pro_be.modules.product.service;

import com.sport_pro_be.modules.brand.domain.Brand;
import com.sport_pro_be.modules.brand.repository.BrandRepository;
import com.sport_pro_be.modules.category.domain.Category;
import com.sport_pro_be.modules.category.repository.CategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.sport_pro_be.common.SlugUtils;
import com.sport_pro_be.modules.audit.annotation.Loggable;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.modules.product.enums.Gender;
import com.sport_pro_be.modules.product.constant.ProductMessageConstant;
import com.sport_pro_be.modules.product.enums.ProductStatus;
import com.sport_pro_be.modules.product.domain.Product;
import com.sport_pro_be.modules.product.domain.ProductImage;
import com.sport_pro_be.modules.product.domain.ProductVariant;
import com.sport_pro_be.modules.product.dto.request.ProductCreateRequest;
import com.sport_pro_be.modules.product.dto.request.ProductUpdateRequest;
import com.sport_pro_be.modules.product.dto.response.ProductDetailResponse;
import com.sport_pro_be.modules.product.dto.response.ProductImageResponse;
import com.sport_pro_be.modules.product.dto.response.ProductListResponse;
import com.sport_pro_be.modules.product.dto.response.ProductVariantResponse;
import com.sport_pro_be.modules.product.interfaces.IProductService;
import com.sport_pro_be.modules.product.repository.ProductRepository;
import com.sport_pro_be.modules.product.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

        private final ProductRepository productRepository;
        private final CategoryRepository categoryRepository;
        private final BrandRepository brandRepository;

        @Override
        @Transactional
        @Loggable(action = "CREATE_PRODUCT", module = "PRODUCT")
        @CacheEvict(value = "products", allEntries = true)
        public ProductDetailResponse createProduct(ProductCreateRequest request) {
                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ProductMessageConstant.CATEGORY_NOT_FOUND));
                Brand brand = brandRepository.findById(request.getBrandId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ProductMessageConstant.BRAND_NOT_FOUND));

                String slug = generateSlug(request.getName());

                Product product = Product.builder()
                                .name(request.getName())
                                .slug(slug)
                                .description(request.getDescription())
                                .category(category)
                                .brand(brand)
                                .gender(request.getGender())
                                .status(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE)
                                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                                .build();

                product = productRepository.save(product);
                return mapToDetailResponse(product);
        }

        @Override
        @Transactional
        @Loggable(action = "UPDATE_PRODUCT", module = "PRODUCT")
        @CacheEvict(value = { "products", "product_details" }, allEntries = true)
        public ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request) {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ProductMessageConstant.PRODUCT_NOT_FOUND));

                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ProductMessageConstant.CATEGORY_NOT_FOUND));
                Brand brand = brandRepository.findById(request.getBrandId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ProductMessageConstant.BRAND_NOT_FOUND));

                if (!product.getName().equals(request.getName())) {
                        product.setName(request.getName());
                        product.setSlug(generateSlug(request.getName()));
                }

                product.setDescription(request.getDescription());
                product.setCategory(category);
                product.setBrand(brand);
                product.setGender(request.getGender());
                product.setStatus(request.getStatus());
                if (request.getIsFeatured() != null) {
                        product.setIsFeatured(request.getIsFeatured());
                }

                product = productRepository.save(product);
                return mapToDetailResponse(product);
        }

        @Override
        @Transactional
        @Loggable(action = "DELETE_PRODUCT", module = "PRODUCT")
        @CacheEvict(value = { "products", "product_details" }, allEntries = true)
        public void deleteProduct(Long id) {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ProductMessageConstant.PRODUCT_NOT_FOUND));
                productRepository.delete(product);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "products", key = "{#keyword, #categoryId, #brandId, #gender, #size, #color, #minPrice, #maxPrice, #isFeatured, #status, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
        public Page<ProductListResponse> getProducts(String keyword, Long categoryId, Long brandId, Gender gender,
                        String size,
                        String color, BigDecimal minPrice, BigDecimal maxPrice, Boolean isFeatured, ProductStatus status,
                        Pageable pageable) {
                Specification<Product> spec = ProductSpecification.filterProducts(keyword, categoryId, brandId, gender,
                                size,
                                color, minPrice, maxPrice, isFeatured, status);
                Page<Product> products = productRepository.findAllWithAssociations(spec, pageable);
                return products.map(this::mapToListResponse);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "product_details", key = "#id")
        public ProductDetailResponse getProductById(Long id) {
                Product product = productRepository.findByIdWithAssociations(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ProductMessageConstant.PRODUCT_NOT_FOUND));
                return mapToDetailResponse(product);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "product_details", key = "#slug")
        public ProductDetailResponse getProductBySlug(String slug) {
                Product product = productRepository.findBySlugWithAssociations(slug)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                ProductMessageConstant.PRODUCT_NOT_FOUND));
                return mapToDetailResponse(product);
        }

        private String generateSlug(String name) {
                String baseSlug = SlugUtils.toSlugBase(name);
                long existingCount = productRepository.countBySlugPattern(baseSlug);
                if (existingCount == 0) {
                        return baseSlug;
                }
                return baseSlug + "-" + (existingCount + 1);
        }

        private ProductListResponse mapToListResponse(Product product) {
                BigDecimal minPrice = null;
                BigDecimal maxPrice = null;
                ProductVariant cheapestVariant = null;
                Set<String> sizes = new LinkedHashSet<>();
                Set<String> colors = new LinkedHashSet<>();
                int totalStock = 0;
                String firstSku = null;

                for (ProductVariant v : product.getVariants()) {
                        BigDecimal price = v.getSalePrice() != null ? v.getSalePrice() : v.getOriginalPrice();
                        if (minPrice == null || price.compareTo(minPrice) < 0) {
                                minPrice = price;
                                cheapestVariant = v;
                        }
                        if (maxPrice == null || price.compareTo(maxPrice) > 0)
                                maxPrice = price;
                        sizes.add(v.getSize());
                        if (v.getColor() != null) {
                                colors.add(v.getColor().getName());
                        } else if (v.getColorOld() != null) {
                                colors.add(v.getColorOld());
                        }
                        totalStock += v.getStockQuantity();
                        if (firstSku == null) {
                                firstSku = v.getSku();
                        }
                }

                String thumbnailUrl = product.getImages().stream()
                                .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                                .map(ProductImage::getImageUrl)
                                .findFirst()
                                .orElseGet(() -> product.getImages().stream()
                                                .map(ProductImage::getImageUrl)
                                                .findFirst()
                                                .orElse(null));

                return ProductListResponse.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .slug(product.getSlug())
                                .thumbnailUrl(thumbnailUrl)
                                .brandName(product.getBrand().getName())
                                .categoryName(product.getCategory().getName())
                                .availableSizes(new ArrayList<>(sizes))
                                .availableColors(new ArrayList<>(colors))
                                .sku(firstSku != null ? firstSku : "N/A")
                                .minPrice(minPrice)
                                .maxPrice(maxPrice)
                                .originalPrice(cheapestVariant != null ? cheapestVariant.getOriginalPrice()
                                                : BigDecimal.ZERO)
                                .salePrice(cheapestVariant != null ? cheapestVariant.getSalePrice() : null)
                                .imageUrl(thumbnailUrl)
                                .gender(product.getGender().name())
                                .status(product.getStatus().name())
                                .totalStock(totalStock)
                                .averageRating(product.getAverageRating())
                                .isFeatured(product.getIsFeatured())
                                .build();
        }

        private ProductDetailResponse mapToDetailResponse(Product product) {
                List<ProductImageResponse> images = product.getImages().stream()
                                .map(img -> ProductImageResponse.builder()
                                                .id(img.getId())
                                                .imageUrl(img.getImageUrl())
                                                .isThumbnail(img.getIsThumbnail())
                                                .sortOrder(img.getSortOrder())
                                                .build())
                                .collect(Collectors.toList());

                List<ProductVariantResponse> variants = product.getVariants().stream()
                                .map(v -> ProductVariantResponse.builder()
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
                                                .build())
                                .collect(Collectors.toList());

                return ProductDetailResponse.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .slug(product.getSlug())
                                .description(product.getDescription())
                                .brandName(product.getBrand().getName())
                                .brandId(product.getBrand().getId())
                                .categoryName(product.getCategory().getName())
                                .categoryId(product.getCategory().getId())
                                .gender(product.getGender())
                                .status(product.getStatus())
                                .images(images)
                                .variants(variants)
                                .isFeatured(product.getIsFeatured())
                                .build();
        }
}
