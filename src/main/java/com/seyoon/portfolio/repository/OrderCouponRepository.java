package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.OrderCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderCouponRepository
        extends JpaRepository<OrderCoupon, Long> {
    Optional<OrderCoupon> findByOrderItem_OrderEntity_OrderId(Long orderId);
}