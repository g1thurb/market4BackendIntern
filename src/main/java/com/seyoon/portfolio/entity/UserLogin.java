package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "user_logins")
public class UserLogin {

    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Column(name = "password_crypted", nullable = false)
    private String passwordCrypted;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uuid", nullable = false)
    private UserInfo userInfo;

    protected UserLogin() {}

    private UserLogin(String id, String passwordCrypted, UserInfo userInfo) {
        this.id = id;
        this.passwordCrypted = passwordCrypted;
        this.userInfo = userInfo;
    }

    public static UserLogin create(String id, String passwordCrypted, UserInfo userInfo){
        return new UserLogin(id, passwordCrypted, userInfo);
    }

    public String getId() {
        return id;
    }

    public String getPasswordCrypted() {
        return passwordCrypted;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }
}