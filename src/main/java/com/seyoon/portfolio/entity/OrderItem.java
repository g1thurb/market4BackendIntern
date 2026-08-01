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

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "item_code", nullable = false)
    private Long itemCode;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price_at_purchase", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceAtPurchase;

    protected  OrderItem() {}

    private OrderItem(Long orderId, Long itemCode, int quantity, BigDecimal unitPriceAtPurchase) {
        this.orderId = orderId;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.unitPriceAtPurchase = unitPriceAtPurchase;
    }

    public static OrderItem create(Long orderId, Long itemCode, int quantity, BigDecimal unitPriceAtPurchase) {
        return new OrderItem(orderId, itemCode, quantity, unitPriceAtPurchase);
    }

    //getter
    public Long getOrderItemId() {return orderItemId;}

    public Long getOrderId() {return orderId;}

    public Long getItemCode() {return itemCode;}

    public int getQuantity() {return quantity;}

    public BigDecimal getUnitPriceAtPurchase() {return unitPriceAtPurchase;}
}
