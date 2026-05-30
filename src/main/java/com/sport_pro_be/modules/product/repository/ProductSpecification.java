package com.sport_pro_be.modules.product.repository;

import com.sport_pro_be.modules.product.enums.Gender;
import com.sport_pro_be.modules.product.enums.ProductStatus;
import com.sport_pro_be.modules.product.domain.Product;
import com.sport_pro_be.modules.product.domain.ProductVariant;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterProducts(
            String keyword, Long categoryId, Long brandId, Gender gender, String size, String color,
            BigDecimal minPrice, BigDecimal maxPrice, Boolean isFeatured, ProductStatus status) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
                Predicate descPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern);
                predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            } else {
                predicates.add(criteriaBuilder.notEqual(root.get("status"), ProductStatus.DELETED));
            }

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (brandId != null) {
                predicates.add(criteriaBuilder.equal(root.get("brand").get("id"), brandId));
            }

            if (gender != null) {
                predicates.add(criteriaBuilder.equal(root.get("gender"), gender));
            }

            if (isFeatured != null) {
                predicates.add(criteriaBuilder.equal(root.get("isFeatured"), isFeatured));
            }

            boolean hasSize = size != null && !size.isBlank();
            boolean hasColor = color != null && !color.isBlank();

            if (hasSize || hasColor || minPrice != null || maxPrice != null) {
                Join<Product, ProductVariant> variantJoin = root.join("variants", JoinType.INNER);

                if (hasSize) {
                    predicates.add(criteriaBuilder.equal(variantJoin.get("size"), size));
                }

                if (hasColor) {
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.equal(variantJoin.get("color").get("name"), color),
                            criteriaBuilder.equal(variantJoin.get("colorOld"), color)
                    ));
                }

                if (minPrice != null) {
                    Expression<BigDecimal> effectivePrice = criteriaBuilder.coalesce(variantJoin.get("salePrice"), variantJoin.get("originalPrice"));
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(effectivePrice, minPrice));
                }

                if (maxPrice != null) {
                    Expression<BigDecimal> effectivePrice = criteriaBuilder.coalesce(variantJoin.get("salePrice"), variantJoin.get("originalPrice"));
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(effectivePrice, maxPrice));
                }
                
                predicates.add(criteriaBuilder.equal(variantJoin.get("status"), ProductStatus.ACTIVE));
                
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}


