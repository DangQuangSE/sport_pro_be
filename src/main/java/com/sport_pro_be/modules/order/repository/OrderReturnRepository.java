package com.sport_pro_be.modules.order.repository;

import com.sport_pro_be.modules.order.domain.OrderReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderReturnRepository extends JpaRepository<OrderReturn, Long> {

    @EntityGraph(attributePaths = {"order", "order.user"})
    Optional<OrderReturn> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    @Override
    @EntityGraph(attributePaths = {"order"})
    Page<OrderReturn> findAll(Pageable pageable);
    
    @Override
    @EntityGraph(attributePaths = {"order", "order.user"})
    Optional<OrderReturn> findById(Long id);
}
