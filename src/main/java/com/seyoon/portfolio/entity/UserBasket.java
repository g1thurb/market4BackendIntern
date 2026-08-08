package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_basket")
public class UserBasket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "basket_id", nullable = false)
    private Long basketId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_uuid", nullable = false)
    private UserInfo userInfo;

    protected UserBasket() {}

    private UserBasket(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public static UserBasket create(UserInfo userInfo) {
        return new UserBasket(userInfo);
    }

    public Long getBasketId() {
        return basketId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }
}