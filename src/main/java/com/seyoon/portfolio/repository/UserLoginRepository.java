package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserLoginRepository extends JpaRepository<UserLogin, String> {

    boolean existsById(String id);

    Optional<UserLogin> findByUuid(UUID uuid);
}