package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository
        extends JpaRepository<Item, Long> {

    @Modifying
    @Query(value = """
        UPDATE items
        SET available = available - :quantity
        WHERE item_code = :itemCode
          AND available >= :quantity
        """, nativeQuery = true)
    int decreaseAvailable(
            @Param("itemCode") long itemCode,
            @Param("quantity") int quantity
    );

}