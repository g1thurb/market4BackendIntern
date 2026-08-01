package com.seyoon.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_payment_saved")
public class UserPaymentSaved {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "payment_encrypted_data",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private Map<String, Object> paymentEncryptedData;

    protected UserPaymentSaved() {
    }

    private UserPaymentSaved(
            UUID uuid,
            Map<String, Object> paymentEncryptedData
    ) {
        this.uuid = uuid;
        this.paymentEncryptedData = paymentEncryptedData;
    }

    public static UserPaymentSaved create(
            UUID uuid,
            Map<String, Object> paymentEncryptedData
    ) {
        return new UserPaymentSaved(uuid, paymentEncryptedData);
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Map<String, Object> getPaymentEncryptedData() {
        return paymentEncryptedData;
    }
}