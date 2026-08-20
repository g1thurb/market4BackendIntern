package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {
}