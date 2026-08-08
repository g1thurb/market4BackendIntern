package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seller_logins")
public class SellerLogin {

    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Column(name = "password_crypted", nullable = false)
    private String passwordCrypted;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_uuid", nullable = false)
    private SellerInfo sellerInfo;

    protected SellerLogin() {}

    private SellerLogin(String id, String passwordCrypted, SellerInfo sellerInfo) {
        this.id = id;
        this.passwordCrypted = passwordCrypted;
        this.sellerInfo = sellerInfo;
    }

    public static SellerLogin create(String id, String passwordCrypted, SellerInfo sellerInfo) {
        return new SellerLogin(id, passwordCrypted, sellerInfo);
    }

    public String getId() {
        return id;
    }

    public String getPasswordCrypted() {
        return passwordCrypted;
    }

    public SellerInfo getSellerInfo() {
        return sellerInfo;
    }
}
