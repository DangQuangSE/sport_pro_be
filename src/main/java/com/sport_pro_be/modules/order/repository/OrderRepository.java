package com.sport_pro_be.modules.order.repository;

import com.sport_pro_be.modules.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.productVariant", "items.productVariant.product"})
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.productVariant", "items.productVariant.product"})
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"items", "items.productVariant", "items.productVariant.product"})
    @org.springframework.data.jpa.repository.Query(
        "SELECT o FROM Order o WHERE " +
        "(:status IS NULL OR o.status = :status) AND " +
        "(:search IS NULL OR o.phoneNumber LIKE %:search% OR CAST(o.id AS string) LIKE %:search% OR o.shippingAddress LIKE %:search%)"
    )
    Page<Order> searchOrders(
            @org.springframework.data.repository.query.Param("search") String search,
            @org.springframework.data.repository.query.Param("status") com.sport_pro_be.modules.order.enums.OrderStatus status,
            Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT new com.sport_pro_be.modules.analytics.dto.RevenueReportResponse(CAST(o.createdAt AS LocalDate), SUM(o.totalAmount)) " +
           "FROM Order o WHERE o.status = 'DELIVERED' " +
           "AND o.createdAt >= :start AND o.createdAt <= :end " +
           "GROUP BY CAST(o.createdAt AS LocalDate) " +
           "ORDER BY CAST(o.createdAt AS LocalDate) ASC")
    java.util.List<com.sport_pro_be.modules.analytics.dto.RevenueReportResponse> calculateDailyRevenue(
            @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    @org.springframework.data.jpa.repository.Query("SELECT o.status, COUNT(o) FROM Order o " +
           "WHERE o.createdAt >= :start AND o.createdAt <= :end " +
           "GROUP BY o.status")
    java.util.List<Object[]> countOrdersByStatus(
            @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
}
