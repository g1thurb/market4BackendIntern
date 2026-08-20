package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.SellerLogin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerLoginRepository extends JpaRepository<SellerLogin, String> {
}