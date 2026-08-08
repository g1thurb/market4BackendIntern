package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity orderEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_code", nullable = false)
    private Item item;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price_at_purchase", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceAtPurchase;

    protected OrderItem() {}

    private OrderItem(OrderEntity orderEntity, Item item, int quantity, BigDecimal unitPriceAtPurchase) {
        this.orderEntity = orderEntity;
        this.item = item;
        this.quantity = quantity;
        this.unitPriceAtPurchase = unitPriceAtPurchase;
    }

    public static OrderItem create(OrderEntity orderEntity, Item item, int quantity, BigDecimal unitPriceAtPurchase) {
        return new OrderItem(orderEntity, item, quantity, unitPriceAtPurchase);
    }

    //getter
    public Long getOrderItemId() {return orderItemId;}

    public OrderEntity getOrderEntity() {return orderEntity;}

    public Item getItem() {return item;}

    public int getQuantity() {return quantity;}

    public BigDecimal getUnitPriceAtPurchase() {return unitPriceAtPurchase;}
}
