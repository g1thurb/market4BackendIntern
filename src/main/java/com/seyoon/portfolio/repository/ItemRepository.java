package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository
        extends JpaRepository<Item, Long> {
}