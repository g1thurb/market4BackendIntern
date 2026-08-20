package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEventRepository
        extends JpaRepository<OrderEvent, Long> {
}