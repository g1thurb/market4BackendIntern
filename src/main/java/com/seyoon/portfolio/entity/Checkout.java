package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "checkouts")
public class Checkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkout_id", nullable = false)
    private Long checkoutId;

    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "checkout_status", nullable = false, length = 30)
    private String checkoutStatus;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Checkout() {}

    private Checkout(UUID userUuid, BigDecimal totalAmount, String checkoutStatus) {
        OffsetDateTime now = OffsetDateTime.now();

        this.userUuid = userUuid;
        this.totalAmount = totalAmount;
        this.checkoutStatus = checkoutStatus;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Checkout create(UUID userUuid, BigDecimal totalAmount, String checkoutStatus) {
        return new Checkout(userUuid, totalAmount, checkoutStatus);
    }

    public Long getCheckoutId() {return checkoutId;}

    public UUID getUserUuid() {return userUuid;}

    public BigDecimal getTotalAmount() {return totalAmount;}

    public String getCheckoutStatus() {return checkoutStatus;}

    public OffsetDateTime getCreatedAt() {return createdAt;}

    public OffsetDateTime getUpdatedAt() {return updatedAt;}
}
