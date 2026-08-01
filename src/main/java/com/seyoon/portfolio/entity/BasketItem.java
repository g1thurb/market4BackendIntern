package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "basket_items")
public class BasketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "basket_item_id", nullable = false)
    private Long basketItemId;

    @Column(name = "basket_id", nullable = false)
    private Long basketId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    protected BasketItem() {}

    private BasketItem(Long basketId, Long itemId, int quantity) {
        this.basketId = basketId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public static BasketItem create(Long basketId, Long itemId, int quantity) {
        return new BasketItem(basketId, itemId, quantity);
    }

    public Long getBasketItemId() {return basketItemId;}

    public Long getBasketId() {return basketId;}

    public Long getItemId() {return itemId;}

    public int getQuantity() {return quantity;}
}
