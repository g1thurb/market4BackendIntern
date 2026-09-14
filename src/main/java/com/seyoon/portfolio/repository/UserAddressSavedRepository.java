package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.BasketItem;
import com.seyoon.portfolio.entity.UserAddressSaved;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressSavedRepository extends JpaRepository<UserAddressSaved, Long> {

    List<UserAddressSaved> findAllByUserInfo_Uuid(UUID userUuid);

    Optional<UserAddressSaved> findByAddressIdAndUserInfo_Uuid(Long addressId, UUID userUuid);
}
