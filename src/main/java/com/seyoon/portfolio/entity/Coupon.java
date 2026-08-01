package com.seyoon.portfolio.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @Column(name = "coupon_code", nullable = false, length = 20)
    private String couponCode;

    @Column(name = "store_uuid", nullable = false)
    private UUID storeUuid;

    @Column(name = "item_code")
    private Long itemCode;

    @Column(name = "discount_type", nullable = false, length = 10)
    private String discountType;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "discount_limit", precision = 12, scale = 2)
    private BigDecimal discountLimit;

    @Column(name = "due_date", nullable = false)
    private OffsetDateTime dueDate;

    protected Coupon() {}

    private Coupon(String couponCode, UUID storeUuid, Long itemCode, String discountType,
                   BigDecimal discountAmount, BigDecimal discountLimit, OffsetDateTime dueDate) {
        this.couponCode = couponCode;
        this.storeUuid = storeUuid;
        this.itemCode = itemCode;
        this.discountType = discountType;
        this.discountAmount = discountAmount;
        this.discountLimit = discountLimit;
        this.dueDate = dueDate;
    }

    public static Coupon create(String couponCode, UUID storeUuid, Long itemCode, String discountType,
                                BigDecimal discountAmount, BigDecimal discountLimit, OffsetDateTime dueDate) {
        return new Coupon(couponCode, storeUuid, itemCode, discountType,  discountAmount, discountLimit, dueDate);
    }

    public String getCouponCode() {return couponCode;}

    public UUID getStoreUuid() {return storeUuid;}

    public Long getItemCode() {return itemCode;}

    public String getDiscountType() {return discountType;}

    public BigDecimal getDiscountAmount() {return discountAmount;}

    public BigDecimal getDiscountLimit() {return discountLimit;}

    public OffsetDateTime getDueDate() {return dueDate;}
}
