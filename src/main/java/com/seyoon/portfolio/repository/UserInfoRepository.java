package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserInfoRepository extends JpaRepository<UserInfo, UUID> {

    boolean existsByNickname(String nickname);

    Optional<UserInfo> findByNickname(String nickname);
}