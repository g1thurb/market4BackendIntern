package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.BasketItem;
import com.seyoon.portfolio.entity.Item;
import com.seyoon.portfolio.entity.UserBasket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BasketItemRepository extends JpaRepository<BasketItem, Long> {

    List<BasketItem> findByUserBasket_BasketId(Long basketId);

    Optional<BasketItem> findByUserBasketAndItem(
            UserBasket userBasket,
            Item item
    );
}