package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_address_saved")
public class UserAddressSaved {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false)
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uuid", nullable = false)
    private UserInfo userInfo;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    protected UserAddressSaved() {
    }

    private UserAddressSaved(UserInfo userInfo, String address) {
        this.userInfo = userInfo;
        this.address = address;
    }

    public static UserAddressSaved create(UserInfo userInfo, String address) {
        return new UserAddressSaved(userInfo, address);
    }

    public Long getAddressId() {
        return addressId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public String getAddress() {
        return address;
    }
}