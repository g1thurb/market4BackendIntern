package com.seyoon.portfolio.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "order_events")
public class OrderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "from_status", length = 30)
    private String fromStatus;

    @Column(name = "to_status", length = 30)
    private String toStatus;

    @Column(name = "actor_type", nullable = false, length = 20)
    private String actorType;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "event_at", nullable = false)
    private OffsetDateTime eventAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> meta = new HashMap<>();

    protected OrderEvent() {}

    private OrderEvent(Long orderId, String eventType, String fromStatus, String toStatus, String actorType, UUID actorId,
                       Map<String, Object> meta) {
        this.orderId = orderId;
        this.eventType = eventType;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actorType = actorType;
        this.actorId = actorId;
        this.eventAt = OffsetDateTime.now();
        this.meta = meta == null ? new HashMap<>() : new HashMap<>(meta); // Defensive copy to prevent external modification.
    }

    public static OrderEvent create(Long orderId, String eventType, String fromStatus, String toStatus, String actorType,
                                    UUID actorId, Map<String, Object> meta) {
        return new OrderEvent(orderId, eventType, fromStatus, toStatus, actorType, actorId, meta);
    }

    public Long getEventId() {return eventId;}
    public Long getOrderId() {return orderId;}
    public String getEventType() {return eventType;}
    public String getFromStatus() {return fromStatus;}
    public String getToStatus() {return toStatus;}
    public String getActorType() {return actorType;}
    public UUID getActorId() {return actorId;}
    public OffsetDateTime getEventAt() {return eventAt;}
    public Map<String, Object> getMeta() {return new HashMap<>(meta);}
}
