package com.seyoon.portfolio.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

import com.seyoon.portfolio.entity.type.OrderStatus;
import com.seyoon.portfolio.entity.type.DeliveredMarkedBy;
import com.seyoon.portfolio.entity.type.ConfirmedBy;

@Entity
@Table(name = "orders")
public class OrderEntity {// Named OrderEntity to avoid confusion with Spring's @Order annotation.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checkout_id", nullable = false)
    private Checkout checkout;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_uuid", nullable = false)
    private UserInfo userInfo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_uuid", nullable = false)
    private SellerInfo sellerInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Column(name = "ordered_at", nullable = false)
    private OffsetDateTime orderedAt;

    @Column(name = "courier_code", length = 20)
    private String courierCode;

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    @Column(name = "delivered_date")
    private OffsetDateTime deliveredDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivered_marked_by", length = 20)
    private DeliveredMarkedBy deliveredMarkedBy;

    @Column(name = "confirm_deadline_date")
    private OffsetDateTime confirmDeadlineDate;

    @Column(name = "confirm_extended", nullable = false)
    private boolean confirmExtended;

    @Column(name = "confirm_date")
    private OffsetDateTime confirmDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "confirmed_by", length = 10)
    private ConfirmedBy confirmedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected OrderEntity() {}

    private OrderEntity(Checkout checkout, UserInfo userInfo, SellerInfo sellerInfo, OrderStatus orderStatus, String courierCode,
                  String trackingNumber,  OffsetDateTime deliveredDate, DeliveredMarkedBy deliveredMarkedBy,
                  OffsetDateTime confirmDeadlineDate, boolean confirmExtended,
                  OffsetDateTime confirmDate, ConfirmedBy confirmedBy) {
        this.checkout = checkout;
        this.userInfo = userInfo;
        this.sellerInfo = sellerInfo;
        this.orderStatus = orderStatus;
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

    public static OrderEntity create(Checkout checkout, UserInfo userInfo, SellerInfo sellerInfo, OrderStatus orderStatus,
                                     String courierCode, String trackingNumber,  OffsetDateTime deliveredDate,
                                     DeliveredMarkedBy deliveredMarkedBy, OffsetDateTime confirmDeadlineDate,
                                     boolean confirmExtended, OffsetDateTime confirmDate, ConfirmedBy confirmedBy) {
        return new OrderEntity(checkout, userInfo, sellerInfo, orderStatus, courierCode, trackingNumber, deliveredDate,
                deliveredMarkedBy, confirmDeadlineDate, confirmExtended, confirmDate, confirmedBy);
    }

    //Getter
    public Long getOrderId() {return orderId;}

    public Checkout getCheckout() {return checkout;}

    public UserInfo getUserInfo() {return userInfo;}

    public SellerInfo getSellerInfo() {return sellerInfo;}

    public OrderStatus getOrderStatus() {return orderStatus;}

    public OffsetDateTime getOrderedAt() {return orderedAt;}

    public String getCourierCode() {return courierCode;}

    public String getTrackingNumber() {return trackingNumber;}

    public OffsetDateTime getDeliveredDate() {return deliveredDate;}

    public DeliveredMarkedBy getDeliveredMarkedBy() {return deliveredMarkedBy;}

    public OffsetDateTime getConfirmDeadlineDate() {return confirmDeadlineDate;}

    public boolean isConfirmExtended() {return confirmExtended;}

    public OffsetDateTime getConfirmDate() {return confirmDate;}

    public ConfirmedBy getConfirmedBy() {return confirmedBy;}

    public Long getVersion() {return version;}
}
