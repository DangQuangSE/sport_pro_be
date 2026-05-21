package com.sport_pro_be.modules.product.repository;

import com.sport_pro_be.modules.product.domain.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Product> findAllWithAssociations(Specification<Product> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> root = cq.from(Product.class);
        root.fetch("brand", JoinType.LEFT);
        root.fetch("category", JoinType.LEFT);

        cq.select(root).distinct(true);

        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, cq, cb);
            if (predicate != null) cq.where(predicate);
        }

        List<Order> orders = new ArrayList<>();
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            java.util.Set<String> attributeNames = root.getModel().getAttributes().stream()
                    .map(jakarta.persistence.metamodel.Attribute::getName)
                    .collect(java.util.stream.Collectors.toSet());

            pageable.getSort().forEach(order -> {
                String property = order.getProperty();
                if (attributeNames.contains(property)) {
                    if (order.isAscending()) {
                        orders.add(cb.asc(root.get(property)));
                    } else {
                        orders.add(cb.desc(root.get(property)));
                    }
                }
            });
        }

        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        } else {
            cq.orderBy(cb.desc(root.get("id")));
        }

        TypedQuery<Product> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        
        int pageSize = Math.min(pageable.getPageSize(), 250);
        query.setMaxResults(pageSize);
        List<Product> content = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(cb.countDistinct(countRoot));
        if (spec != null) {
            Predicate countPredicate = spec.toPredicate(countRoot, countQuery, cb);
            if (countPredicate != null) countQuery.where(countPredicate);
        }
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}