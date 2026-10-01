package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findAllByOrderEntity_OrderId(Long orderId);
}