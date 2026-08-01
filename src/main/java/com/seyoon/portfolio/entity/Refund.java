package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id", nullable = false)
    private Long refundId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "refund_type", nullable = false, length = 30)
    private String refundType;

    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "deduction_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal deductionAmount;

    @Column(name = "deduction_reason", length = 50)
    private String deductionReason;

    @Column(name = "refund_status", nullable = false, length = 20)
    private String refundStatus;

    @Column(name = "requested_by_type", nullable = false, length = 20)
    private String requestedByType;

    @Column(name = "requested_by_uuid")
    private UUID requestedByUuid;

    @Column(name = "reason", length = 200)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "done_at")
    private OffsetDateTime doneAt;

    @Column(name = "provider_refund_id", length = 100)
    private String providerRefundId;

    protected Refund() {}

    private Refund(Long paymentId, Long orderId, String refundType, BigDecimal refundAmount, BigDecimal deductionAmount,
                   String deductionReason, String refundStatus, String requestedByType, UUID requestedByUuid, String reason,
                   OffsetDateTime doneAt, String providerRefundId) {
        OffsetDateTime now =  OffsetDateTime.now();

        this.paymentId = paymentId;
        this.orderId = orderId;
        this.refundType = refundType;
        this.refundAmount = refundAmount;
        this.deductionAmount = deductionAmount;
        this.deductionReason = deductionReason;
        this.refundStatus = refundStatus;
        this.requestedByType = requestedByType;
        this.requestedByUuid = requestedByUuid;
        this.reason = reason;
        this.createdAt = now;
        this.doneAt = doneAt;
        this.providerRefundId = providerRefundId;
    }

    public static Refund create(Long paymentId, Long orderId, String refundType, BigDecimal refundAmount, BigDecimal deductionAmount,
                                String deductionReason, String refundStatus, String requestedByType, UUID requestedByUuid, String reason,
                                OffsetDateTime doneAt, String providerRefundId) {
        return new Refund(paymentId, orderId, refundType, refundAmount, deductionAmount, deductionReason, refundStatus,
                requestedByType, requestedByUuid, reason, doneAt, providerRefundId);
    }

    public Long getRefundId() {return refundId;}

    public Long getPaymentId() {return paymentId;}

    public Long getOrderId() {return orderId;}

    public String getRefundType() {return refundType;}

    public BigDecimal getRefundAmount() {return refundAmount;}

    public BigDecimal getDeductionAmount() {return deductionAmount;}

    public String getDeductionReason() {return deductionReason;}

    public String getRefundStatus() {return refundStatus;}

    public String getRequestedByType() {return requestedByType;}

    public UUID getRequestedByUuid() {return requestedByUuid;}

    public String getReason() {return reason;}

    public OffsetDateTime getCreatedAt() {return createdAt;}

    public OffsetDateTime getDoneAt() {return doneAt;}

    public String getProviderRefundId() {return providerRefundId;}
}