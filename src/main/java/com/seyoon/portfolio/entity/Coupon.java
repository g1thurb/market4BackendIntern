package com.seyoon.portfolio.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.seyoon.portfolio.entity.type.DiscountType;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @Column(name = "coupon_code", nullable = false, length = 20)
    private String couponCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_uuid", nullable = false)
    private SellerInfo sellerInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_code")
    private Item item;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 10)
    private DiscountType discountType;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "discount_limit", precision = 12, scale = 2)
    private BigDecimal discountLimit;

    @Column(name = "due_date", nullable = false)
    private OffsetDateTime dueDate;

    protected Coupon() {}

    private Coupon(String couponCode, SellerInfo sellerInfo, Item item, DiscountType discountType,
                   BigDecimal discountAmount, BigDecimal discountLimit, OffsetDateTime dueDate) {
        this.couponCode = couponCode;
        this.sellerInfo = sellerInfo;
        this.item =  item;
        this.discountType = discountType;
        this.discountAmount = discountAmount;
        this.discountLimit = discountLimit;
        this.dueDate = dueDate;
    }

    public static Coupon create(String couponCode, SellerInfo sellerInfo, Item item, DiscountType discountType,
                                BigDecimal discountAmount, BigDecimal discountLimit, OffsetDateTime dueDate) {
        return new Coupon(couponCode, sellerInfo, item, discountType,  discountAmount, discountLimit, dueDate);
    }

    public String getCouponCode() {return couponCode;}

    public SellerInfo getSellerInfo() {return sellerInfo;}

    public Item getItem() {return item;}

    public DiscountType getDiscountType() {return discountType;}

    public BigDecimal getDiscountAmount() {return discountAmount;}

    public BigDecimal getDiscountLimit() {return discountLimit;}

    public OffsetDateTime getDueDate() {return dueDate;}
}
