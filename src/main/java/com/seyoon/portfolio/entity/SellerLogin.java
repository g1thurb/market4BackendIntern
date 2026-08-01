package com.seyoon.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "seller_logins")
public class SellerLogin {

    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Column(name = "password_crypted", nullable = false)
    private String passwordCrypted;

    @Column(name = "store_uuid", nullable = false, unique = true)
    private UUID storeUuid;

    protected SellerLogin() {}

    private SellerLogin(String id, String passwordCrypted, UUID storeUuid) {
        this.id = id;
        this.passwordCrypted = passwordCrypted;
        this.storeUuid = storeUuid;
    }

    public static SellerLogin create(String id, String passwordCrypted, UUID storeUuid) {
        return new SellerLogin(id, passwordCrypted, storeUuid);
    }

    public String getId() {
        return id;
    }

    public String getPasswordCrypted() {
        return passwordCrypted;
    }

    public UUID getStoreUuid() {
        return storeUuid;
    }
}
