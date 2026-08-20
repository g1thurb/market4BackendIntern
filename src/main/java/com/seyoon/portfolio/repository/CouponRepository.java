package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository
        extends JpaRepository<Coupon, String> {
}