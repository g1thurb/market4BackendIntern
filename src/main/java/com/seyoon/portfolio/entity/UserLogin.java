package com.seyoon.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "user_logins")
public class UserLogin {

    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Column(name = "password_crypted", nullable = false)
    private String passwordCrypted;

    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    protected UserLogin() {}

    private UserLogin(String id, String passwordCrypted, UUID uuid) {
        this.id = id;
        this.passwordCrypted = passwordCrypted;
        this.uuid = uuid;
    }

    public static UserLogin create(String id, String passwordCrypted, UUID uuid){
        return new  UserLogin(id, passwordCrypted, uuid);
    }

    public String getId() {
        return id;
    }

    public String getPasswordCrypted() {
        return passwordCrypted;
    }

    public UUID getUuid() {
        return uuid;
    }
}