package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {
}