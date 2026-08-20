package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.OrderCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderCouponRepository
        extends JpaRepository<OrderCoupon, Long> {
}