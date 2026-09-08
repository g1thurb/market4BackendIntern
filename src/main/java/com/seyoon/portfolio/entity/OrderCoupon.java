package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import com.seyoon.portfolio.entity.type.DiscountTypeSnapshot;

@Entity
@Table(name = "order_coupons")
public class OrderCoupon {

    @Id
    @Column(name = "order_item_id")
    private Long orderItemId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_code", nullable = false)
    private  Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type_snapshot", nullable = false, length = 10)
    private  DiscountTypeSnapshot discountTypeSnapshot;

    @Column(name = "discount_amount_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmountSnapshot;

    @Column(name = "discount_limit_snapshot", precision = 12, scale = 2)
    private BigDecimal discountLimitSnapshot;

    @Column(name = "applied_discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal appliedDiscountAmount;

    protected  OrderCoupon() {}

    private OrderCoupon(OrderItem orderItem, Coupon coupon,DiscountTypeSnapshot discountTypeSnapshot, BigDecimal discountAmountSnapshot,
                        BigDecimal discountLimitSnapshot, BigDecimal appliedDiscountAmount) {
        this.orderItem = orderItem;
        this.coupon = coupon;
        this.discountTypeSnapshot = discountTypeSnapshot;
        this.discountAmountSnapshot = discountAmountSnapshot;
        this.discountLimitSnapshot = discountLimitSnapshot;
        this.appliedDiscountAmount = appliedDiscountAmount;
    }

    public static OrderCoupon create(OrderItem orderItem, Coupon coupon, DiscountTypeSnapshot discountTypeSnapshot,
                                     BigDecimal discountAmountSnapshot, BigDecimal discountLimitSnapshot,
                                     BigDecimal appliedDiscountAmount) {
        return new OrderCoupon(orderItem, coupon, discountTypeSnapshot, discountAmountSnapshot,
                                discountLimitSnapshot, appliedDiscountAmount);
    }

    public OrderItem getOrderItem() {return orderItem;}

    public Coupon getCoupon() {return coupon;}

    public DiscountTypeSnapshot getDiscountTypeSnapshot() {return discountTypeSnapshot;}

    public BigDecimal getDiscountAmountSnapshot() {return discountAmountSnapshot;}

    public BigDecimal getDiscountLimitSnapshot() {return discountLimitSnapshot;}

    public BigDecimal getAppliedDiscountAmount() {return appliedDiscountAmount;}
}
