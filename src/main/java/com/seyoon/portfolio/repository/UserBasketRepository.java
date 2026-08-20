package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.UserBasket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserBasketRepository extends JpaRepository<UserBasket, Long> {

    Optional<UserBasket> findByUserInfo_Uuid(UUID uuid);
}