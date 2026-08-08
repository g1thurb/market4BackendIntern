package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.seyoon.portfolio.entity.type.CheckoutStatus;

@Entity
@Table(name = "checkouts")
public class Checkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkout_id", nullable = false)
    private Long checkoutId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_uuid", nullable = false)
    private UserInfo userInfo;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "checkout_status", nullable = false, length = 30)
    private CheckoutStatus checkoutStatus;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Checkout() {}

    private Checkout(UserInfo userInfo, BigDecimal totalAmount, CheckoutStatus checkoutStatus) {
        OffsetDateTime now = OffsetDateTime.now();

        this.userInfo = userInfo;
        this.totalAmount = totalAmount;
        this.checkoutStatus = checkoutStatus;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Checkout create(UserInfo userInfo, BigDecimal totalAmount, CheckoutStatus checkoutStatus) {
        return new Checkout(userInfo, totalAmount, checkoutStatus);
    }

    public Long getCheckoutId() {return checkoutId;}

    public UserInfo getUserInfo() {return userInfo;}

    public BigDecimal getTotalAmount() {return totalAmount;}

    public CheckoutStatus getCheckoutStatus() {return checkoutStatus;}

    public OffsetDateTime getCreatedAt() {return createdAt;}

    public OffsetDateTime getUpdatedAt() {return updatedAt;}
}
