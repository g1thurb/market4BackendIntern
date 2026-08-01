package com.seyoon.portfolio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "seller_info")
public class SellerInfo {

    @Id
    @Column(name = "store_uuid", nullable = false)
    private UUID storeUuid;

    @Column(name = "store_name", nullable = false, unique = true, length = 100)
    private String storeName;

    @Column(name = "generated_date", nullable = false)
    private LocalDate generatedDate;

    @Column(name = "store_tin", unique = true, length = 20)
    private String storeTin;

    @Column(name = "store_crn", unique = true, length = 20)
    private String storeCrn;

    @Column(name = "store_brn", unique = true, length = 20)
    private String storeBrn;

    protected SellerInfo() {}

    private SellerInfo(UUID storeUuid, String storeName, LocalDate generatedDate, String storeTin, String storeCrn, String storeBrn) {
        this.storeUuid = storeUuid;
        this.storeName = storeName;
        this.generatedDate = generatedDate;
        this.storeTin = storeTin;
        this.storeCrn = storeCrn;
        this.storeBrn = storeBrn;
    }

    public static SellerInfo create(String storeName, String storeTin, String storeCrn, String storeBrn) {
        return new SellerInfo(UUID.randomUUID(), storeName, LocalDate.now(), storeTin, storeCrn, storeBrn);
    }

    public void changeStoreName(String newStoreName) {this.storeName = newStoreName;}

    public UUID getStoreUuid() {return storeUuid;}

    public String getStoreName() {return storeName;}

    public String getStoreTin() {return storeTin;}

    public String getStoreCrn() {return storeCrn;}

    public String getStoreBrn() {return storeBrn;}

    public LocalDate getGeneratedDate() {return generatedDate;}
}
