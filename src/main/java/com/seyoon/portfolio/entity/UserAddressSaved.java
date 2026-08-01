package com.seyoon.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "user_address_saved")
public class UserAddressSaved {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id", nullable = false)
    private Long addressId;

    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    protected UserAddressSaved() {
    }

    private UserAddressSaved(UUID uuid, String address) {
        this.uuid = uuid;
        this.address = address;
    }

    public static UserAddressSaved create(UUID uuid, String address) {
        return new UserAddressSaved(uuid, address);
    }

    public Long getAddressId() {
        return addressId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getAddress() {
        return address;
    }
}