package com.seyoon.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_info")
public class UserInfo {

    @Id
    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "nickname", nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @Column(name = "email", length = 254)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "default_address_id")
    private Long defaultAddressId;

    protected UserInfo() {}

    private UserInfo(UUID uuid, String nickname, String username, LocalDate birthday, String email, String phone) {
        this.uuid = uuid;
        this.nickname = nickname;
        this.username = username;
        this.birthday = birthday;
        this.email = email;
        this.phone = phone;
    }

    public static UserInfo create(UUID uuid, String nickname, String username, LocalDate birthday, String email, String phone) {
        return new UserInfo(uuid, nickname, username, birthday, email, phone);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUsername() {
        return username;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Long getDefaultAddressId() {
        return defaultAddressId;
    }
}