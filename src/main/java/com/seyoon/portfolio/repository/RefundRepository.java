package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository
        extends JpaRepository<Refund, Long> {
}