package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.UserPaymentSaved;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPaymentSavedRepository extends JpaRepository<UserPaymentSaved, Long> {
}
