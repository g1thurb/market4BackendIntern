package com.seyoon.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "user_basket")
public class UserBasket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "basket_id", nullable = false)
    private Long basketId;

    @Column(name = "user_uuid", nullable = false, unique = true)
    private UUID userUuid;

    protected UserBasket() {}

    private UserBasket(UUID userUuid) {
        this.userUuid = userUuid;
    }

    public static UserBasket create(UUID userUuid) {
        return new UserBasket(userUuid);
    }

    public Long getBasketId() {
        return basketId;
    }

    public UUID getUserUuid() {
        return userUuid;
    }
}