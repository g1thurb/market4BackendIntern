package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.Checkout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckoutRepository
        extends JpaRepository<Checkout, Long> {
}