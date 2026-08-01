package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {// Named OrderEntity to avoid confusion with Spring's @Order annotation.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "checkout_id", nullable = false)
    private Long checkoutId;

    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid;

    @Column(name = "store_uuid", nullable = false)
    private UUID storeUuid;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "ordered_at", nullable = false)
    private OffsetDateTime orderedAt;

    @Column(name = "courier_code", length = 20)
    private String courierCode;

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    @Column(name = "delivered_date")
    private OffsetDateTime deliveredDate;

    @Column(name = "delivered_marked_by", length = 20)
    private String deliveredMarkedBy;

    @Column(name = "confirm_deadline_date")
    private OffsetDateTime confirmDeadlineDate;

    @Column(name = "confirm_extended", nullable = false)
    private boolean confirmExtended;

    @Column(name = "confirm_date")
    private OffsetDateTime confirmDate;

    @Column(name = "confirmed_by", length = 10)
    private String confirmedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected OrderEntity() {}

    private OrderEntity(Long checkoutId, UUID userUuid, UUID storeUuid, String status, String courierCode,
                  String trackingNumber,  OffsetDateTime deliveredDate, String deliveredMarkedBy,
                  OffsetDateTime confirmDeadlineDate, boolean confirmExtended,
                  OffsetDateTime confirmDate, String confirmedBy) {
        this.checkoutId = checkoutId;
        this.userUuid = userUuid;
        this.storeUuid = storeUuid;
        this.status = status;
        this.orderedAt = OffsetDateTime.now();
        this.courierCode = courierCode;
        this.trackingNumber = trackingNumber;
        this.deliveredDate = deliveredDate;
        this.deliveredMarkedBy = deliveredMarkedBy;
        this.confirmDate = confirmDate;
        this.confirmDeadlineDate = confirmDeadlineDate;
        this.confirmExtended = confirmExtended;
        this.confirmedBy = confirmedBy;
    }

    public static OrderEntity create(Long checkoutId, UUID userUuid, UUID storeUuid, String status, String courierCode,
                               String trackingNumber,  OffsetDateTime deliveredDate, String deliveredMarkedBy,
                               OffsetDateTime confirmDeadlineDate, boolean confirmExtended,
                               OffsetDateTime confirmDate, String confirmedBy) {
        return new OrderEntity(checkoutId, userUuid, storeUuid, status, courierCode, trackingNumber, deliveredDate,
                deliveredMarkedBy, confirmDeadlineDate, confirmExtended, confirmDate, confirmedBy);
    }

    //Getter
    public Long getOrderId() {return orderId;}

    public Long getCheckoutId() {return checkoutId;}

    public UUID getUserUuid() {return userUuid;}

    public UUID getStoreUuid() {return storeUuid;}

    public String getStatus() {return status;}

    public OffsetDateTime getOrderedAt() {return orderedAt;}

    public String getCourierCode() {return courierCode;}

    public String getTrackingNumber() {return trackingNumber;}

    public OffsetDateTime getDeliveredDate() {return deliveredDate;}

    public String getDeliveredMarkedBy() {return deliveredMarkedBy;}

    public OffsetDateTime getConfirmDeadlineDate() {return confirmDeadlineDate;}

    public boolean isConfirmExtended() {return confirmExtended;}

    public OffsetDateTime getConfirmDate() {return confirmDate;}

    public String getConfirmedBy() {return confirmedBy;}

    public Long getVersion() {return version;}
}
