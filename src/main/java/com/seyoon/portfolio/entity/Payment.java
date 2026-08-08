package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.seyoon.portfolio.entity.type.PaymentStatus;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checkout_id", nullable = false)
    private Checkout checkout;

    @Column(name = "payment_provider", nullable = false, length = 30)
    private String paymentProvider;

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    @Column(name = "installment_months", nullable = false)
    private short installmentMonths;

    @Column(name = "amount_authorized", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountAuthorized;

    @Column(name = "amount_captured", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountCaptured;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Column(name = "provider_tx_id", length = 100)
    private String providerTxId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Payment() {}

    private Payment(Checkout checkout, String paymentProvider, String paymentMethod, short installmentMonths,
                    BigDecimal amountAuthorized, BigDecimal amountCaptured, PaymentStatus paymentStatus, String providerTxId) {
        OffsetDateTime now = OffsetDateTime.now();

        this.checkout = checkout;
        this.paymentProvider = paymentProvider;
        this.paymentMethod = paymentMethod;
        this.installmentMonths = installmentMonths;
        this.amountAuthorized = amountAuthorized;
        this.amountCaptured = amountCaptured;
        this.paymentStatus = paymentStatus;
        this.providerTxId = providerTxId;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Payment create(Checkout checkout, String paymentProvider, String paymentMethod, short installmentMonths,
                                 BigDecimal amountAuthorized, BigDecimal amountCaptured, PaymentStatus paymentStatus, String providerTxId){
        return new Payment(checkout, paymentProvider, paymentMethod, installmentMonths, amountAuthorized,
                amountCaptured, paymentStatus, providerTxId);
    }

    public Long getPaymentId() {return paymentId;}

    public Checkout getCheckout() {return checkout;}

    public String getPaymentProvider() {return paymentProvider;}

    public String getPaymentMethod() {return paymentMethod;}

    public short getInstallmentMonths() {return installmentMonths;}

    public BigDecimal getAmountAuthorized() {return amountAuthorized;}

    public BigDecimal getAmountCaptured() {return amountCaptured;}

    public PaymentStatus getPaymentStatus() {return paymentStatus;}

    public String getProviderTxId() {return providerTxId;}

    public OffsetDateTime getCreatedAt() {return createdAt;}

    public OffsetDateTime getUpdatedAt() {return updatedAt;}
}