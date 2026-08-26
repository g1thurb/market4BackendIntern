package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.UserAddressSaved;
import com.seyoon.portfolio.entity.UserPaymentSaved;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserPaymentSavedRepository extends JpaRepository<UserPaymentSaved, Long> {

    List<UserPaymentSaved> findAllByUserInfo_Uuid(UUID userUuid);
}
