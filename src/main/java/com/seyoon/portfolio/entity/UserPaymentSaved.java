package com.seyoon.portfolio.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "user_payment_saved")
public class UserPaymentSaved {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uuid", nullable = false)
    private UserInfo userInfo;

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
            UserInfo userInfo,
            Map<String, Object> paymentEncryptedData
    ) {
        this.userInfo = userInfo;
        this.paymentEncryptedData = new HashMap<>(paymentEncryptedData);
    }

    public static UserPaymentSaved create(
            UserInfo userInfo,
            Map<String, Object> paymentEncryptedData
    ) {
        return new UserPaymentSaved(userInfo, paymentEncryptedData);
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public Map<String, Object> getPaymentEncryptedData() {
        return new HashMap<>(paymentEncryptedData);
    }
    //paymentEncryptedData == null이면 NullPointerException이 나니까, 그때 null을 아예 금지할지 아니면 빈 Map으로 바꿀지 정책을 정하기
}