package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository
        extends JpaRepository<OrderEntity, Long> {
}