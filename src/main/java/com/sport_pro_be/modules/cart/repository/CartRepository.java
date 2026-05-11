package com.sport_pro_be.modules.cart.repository;

import com.sport_pro_be.modules.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    @EntityGraph(attributePaths = {"items", "items.productVariant", "items.productVariant.product"})
    Optional<Cart> findByUserId(Long userId);
}
