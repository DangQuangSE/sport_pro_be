package com.sport_pro_be.modules.order.repository;

import com.sport_pro_be.modules.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @org.springframework.data.jpa.repository.Query("SELECT new com.sport_pro_be.modules.analytics.dto.TopProductResponse(p.id, p.name, SUM(oi.quantity)) " +
           "FROM OrderItem oi " +
           "JOIN oi.productVariant pv " +
           "JOIN pv.product p " +
           "GROUP BY p.id, p.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    java.util.List<com.sport_pro_be.modules.analytics.dto.TopProductResponse> findTopSellingProducts(org.springframework.data.domain.Pageable pageable);
}
