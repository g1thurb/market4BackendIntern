package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.SellerInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SellerInfoRepository
        extends JpaRepository<SellerInfo, UUID> {
}