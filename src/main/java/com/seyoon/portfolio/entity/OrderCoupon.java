package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import com.seyoon.portfolio.entity.type.DiscountTypeSnapshot;

@Entity
@Table(name = "order_coupons")
public class OrderCoupon {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId//이 Entity의 PK를 저 연관 객체의 PK와 똑같이 써라
    @JoinColumn(name = "order_id")
    private OrderEntity orderEntity;

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

    private OrderCoupon(OrderEntity orderEntity, Coupon coupon,DiscountTypeSnapshot discountTypeSnapshot, BigDecimal discountAmountSnapshot,
                        BigDecimal discountLimitSnapshot, BigDecimal appliedDiscountAmount) {
        this.orderEntity = orderEntity;
        this.coupon = coupon;
        this.discountTypeSnapshot = discountTypeSnapshot;
        this.discountAmountSnapshot = discountAmountSnapshot;
        this.discountLimitSnapshot = discountLimitSnapshot;
        this.appliedDiscountAmount = appliedDiscountAmount;
    }

    public static OrderCoupon create(OrderEntity orderEntity, Coupon coupon, DiscountTypeSnapshot discountTypeSnapshot,
                                     BigDecimal discountAmountSnapshot, BigDecimal discountLimitSnapshot,
                                     BigDecimal appliedDiscountAmount) {
        return new OrderCoupon(orderEntity, coupon, discountTypeSnapshot, discountAmountSnapshot,
                                discountLimitSnapshot, appliedDiscountAmount);
    }

    public OrderEntity getOrderEntity() {return orderEntity;}

    public Coupon getCoupon() {return coupon;}

    public DiscountTypeSnapshot getDiscountTypeSnapshot() {return discountTypeSnapshot;}

    public BigDecimal getDiscountAmountSnapshot() {return discountAmountSnapshot;}

    public BigDecimal getDiscountLimitSnapshot() {return discountLimitSnapshot;}

    public BigDecimal getAppliedDiscountAmount() {return appliedDiscountAmount;}
}
