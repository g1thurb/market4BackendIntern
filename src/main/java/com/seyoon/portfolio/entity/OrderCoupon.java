package com.seyoon.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "order_coupons")
public class OrderCoupon {

    @Id
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "coupon_code", nullable = false, length = 20)
    private  String couponCode;

    @Column(name = "discount_type_snapshot", nullable = false, length = 10)
    private  String discountTypeSnapshot;

    @Column(name = "discount_amount_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmountSnapshot;

    @Column(name = "discount_limit_snapshot", precision = 12, scale = 2)
    private BigDecimal discountLimitSnapshot;

    @Column(name = "applied_discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal appliedDiscountAmount;

    protected  OrderCoupon() {}

    private OrderCoupon(Long orderId, String couponCode,String discountTypeSnapshot, BigDecimal discountAmountSnapshot,
                        BigDecimal discountLimitSnapshot, BigDecimal appliedDiscountAmount) {
        this.orderId = orderId;
        this.couponCode = couponCode;
        this.discountTypeSnapshot = discountTypeSnapshot;
        this.discountAmountSnapshot = discountAmountSnapshot;
        this.discountLimitSnapshot = discountLimitSnapshot;
        this.appliedDiscountAmount = appliedDiscountAmount;
    }

    public static OrderCoupon create(Long orderId, String couponCode, String discountTypeSnapshot, BigDecimal discountAmountSnapshot,
                                     BigDecimal discountLimitSnapshot, BigDecimal appliedDiscountAmount) {
        return new OrderCoupon(orderId, couponCode, discountTypeSnapshot, discountAmountSnapshot,
                                discountLimitSnapshot, appliedDiscountAmount);
    }

    public Long getOrderId() {return orderId;}

    public String getCouponCode() {return couponCode;}

    public String getDiscountTypeSnapshot() {return discountTypeSnapshot;}

    public BigDecimal getDiscountAmountSnapshot() {return discountAmountSnapshot;}

    public BigDecimal getDiscountLimitSnapshot() {return discountLimitSnapshot;}

    public BigDecimal getAppliedDiscountAmount() {return appliedDiscountAmount;}
}
