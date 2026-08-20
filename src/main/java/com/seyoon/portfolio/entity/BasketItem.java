package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "basket_items")
public class BasketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "basket_item_id", nullable = false)
    private Long basketItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "basket_id", nullable = false)
    private UserBasket userBasket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    protected BasketItem() {}

    private BasketItem(UserBasket userBasket, Item item, int quantity) {
        this.userBasket = userBasket;
        this.item = item;
        this.quantity = quantity;
    }

    public static BasketItem create(UserBasket userBasket, Item item, int quantity) {
        return new BasketItem(userBasket, item, quantity);
    }

    public Long getBasketItemId() {return basketItemId;}

    public UserBasket getUserBasket() {return userBasket;}

    public Item getItem() {return item;}

    public int getQuantity() {return quantity;}

    public void changeQuantity(int quantity) {
        if(quantity>0) {this.quantity = quantity;}
        else {throw new IllegalArgumentException("Quantity must be greater than zero");}
    }
}
