package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLoginRepository
        extends JpaRepository<UserLogin, String> {
}