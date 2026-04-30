package com.sport_pro_be.product.service;

import com.sport_pro_be.brand.domain.Brand;
import com.sport_pro_be.brand.repository.BrandRepository;
import com.sport_pro_be.category.domain.Category;
import com.sport_pro_be.category.repository.CategoryRepository;
import com.sport_pro_be.common.SlugUtils;
import com.sport_pro_be.exception.ResourceNotFoundException;
import com.sport_pro_be.product.constant.Gender;
import com.sport_pro_be.product.constant.ProductStatus;
import com.sport_pro_be.product.domain.Product;
import com.sport_pro_be.product.domain.ProductImage;
import com.sport_pro_be.product.domain.ProductVariant;
import com.sport_pro_be.product.dto.request.ProductCreateRequest;
import com.sport_pro_be.product.dto.request.ProductUpdateRequest;
import com.sport_pro_be.product.dto.response.ProductDetailResponse;
import com.sport_pro_be.product.dto.response.ProductImageResponse;
import com.sport_pro_be.product.dto.response.ProductListResponse;
import com.sport_pro_be.product.dto.response.ProductVariantResponse;
import com.sport_pro_be.product.interfaces.IProductService;
import com.sport_pro_be.product.repository.ProductRepository;
import com.sport_pro_be.product.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Override
    @Transactional
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        String slug = generateSlug(request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .category(category)
                .brand(brand)
                .gender(request.getGender())
                .status(ProductStatus.ACTIVE)
                .build();

        product = productRepository.save(product);
        return mapToDetailResponse(product);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        if (!product.getName().equals(request.getName())) {
            product.setName(request.getName());
            product.setSlug(generateSlug(request.getName()));
        }

        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBrand(brand);
        product.setGender(request.getGender());
        product.setStatus(request.getStatus());

        product = productRepository.save(product);
        return mapToDetailResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductListResponse> getProducts(Long categoryId, Long brandId, Gender gender, String size, String color, BigDecimal minPrice, BigDecimal maxPrice, ProductStatus status, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.filterProducts(categoryId, brandId, gender, size, color, minPrice, maxPrice, status);
        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(this::mapToListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapToDetailResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapToDetailResponse(product);
    }

    private String generateSlug(String name) {
        String baseSlug = SlugUtils.toSlugBase(name);
        String slug = baseSlug;
        int count = 1;
        while (productRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + count++;
        }
        return slug;
    }

    private ProductListResponse mapToListResponse(Product product) {
        BigDecimal minPrice = product.getVariants().stream()
                .map(ProductVariant::getPrice)
                .min(BigDecimal::compareTo).orElse(null);
        BigDecimal maxPrice = product.getVariants().stream()
                .map(ProductVariant::getPrice)
                .max(BigDecimal::compareTo).orElse(null);

        List<String> sizes = product.getVariants().stream()
                .map(ProductVariant::getSize).distinct().collect(Collectors.toList());
        List<String> colors = product.getVariants().stream()
                .map(ProductVariant::getColor).distinct().collect(Collectors.toList());

        String thumbnailUrl = product.getImages().stream()
                .filter(ProductImage::getIsThumbnail)
                .map(ProductImage::getImageUrl)
                .findFirst().orElse(null);

        return ProductListResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .thumbnailUrl(thumbnailUrl)
                .brandName(product.getBrand().getName())
                .categoryName(product.getCategory().getName())
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .availableSizes(sizes)
                .availableColors(colors)
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
                        .color(v.getColor())
                        .price(v.getPrice())
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
                .categoryName(product.getCategory().getName())
                .gender(product.getGender())
                .images(images)
                .variants(variants)
                .build();
    }
}
