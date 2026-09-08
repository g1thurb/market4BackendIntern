package com.seyoon.portfolio.entity;

import com.seyoon.portfolio.exception.InsufficientStockException;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_code", nullable = false)
    private Long itemCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_uuid", nullable = false)
    private SellerInfo sellerInfo;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "upload_date", nullable = false)
    private OffsetDateTime uploadDate;

    @Column(name = "delivery_type", nullable = false, length = 30)
    private String deliveryType;//복잡한게 많아 보여서 프로그램 내에서 자동완성 저장 해뒀다가 추후에 data 보고 enum 여부 결정

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "main_images", nullable = false, columnDefinition = "jsonb")
    private List<String> mainImages = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column( name = "describe_file", columnDefinition = "jsonb")
    private Map<String, Object> describeFile;

    @Column(name = "available", nullable = false)
    private int available;

    protected Item() {}

    private Item(SellerInfo sellerInfo, String itemName, BigDecimal price, OffsetDateTime uploadDate, String deliveryType,
                 List<String> mainImages, Map<String, Object> describeFile, int available) {
        this.sellerInfo = sellerInfo;
        this.itemName = itemName;
        this.price = price;
        this.uploadDate = uploadDate;
        this.deliveryType = deliveryType;
        this.mainImages = mainImages == null ? new ArrayList<>() : new ArrayList<>(mainImages);
        this.describeFile = describeFile == null ? null : new HashMap<>(describeFile);
        if (available < 0) {
            throw new IllegalArgumentException("재고는 음수일 수 없습니다.");
        }
        this.available = available;
    }

    public static Item create(SellerInfo sellerInfo, String itemName, BigDecimal price, String deliveryType,
                              List<String> mainImages, Map<String, Object> describeFile, int available) {
        return new Item(sellerInfo, itemName, price, OffsetDateTime.now(), deliveryType, mainImages, describeFile, available);
    }

    public Long getItemCode() {return itemCode;}

    public SellerInfo getSellerInfo() {return sellerInfo;}

    public String getItemName() {return itemName;}

    public BigDecimal getPrice() {return price;}

    public OffsetDateTime getUploadDate() {return uploadDate;}

    public String getDeliveryType() {return deliveryType;}

    public void addMainImage(String imageUrl) {mainImages.add(imageUrl);}

    public List<String> getMainImages() {return new ArrayList<>(mainImages);}
    // Prevent external modification of the entity's internal list.

    public Map<String, Object> getDescribeFile() {
        return describeFile == null ? null : new HashMap<>(describeFile);
    }// Prevent external modification of the entity's internal map.

    public int getAvailable() {return available;}

    public boolean subtractAvailable(int amount) {
        if (amount <= 0 || amount > available) {
            return false;
        }
        available -= amount;
        return true;
    }
}
