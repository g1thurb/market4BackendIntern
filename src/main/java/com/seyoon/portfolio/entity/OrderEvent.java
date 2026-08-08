package com.seyoon.portfolio.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.seyoon.portfolio.entity.type.EventType;
import com.seyoon.portfolio.entity.type.OrderStatus;
import com.seyoon.portfolio.entity.type.ActorType;

@Entity
@Table(name = "order_events")
public class OrderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity orderEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 30)
    private OrderStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 20)
    private ActorType actorType;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "event_at", nullable = false)
    private OffsetDateTime eventAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> meta = new HashMap<>();

    protected OrderEvent() {}

    private OrderEvent(OrderEntity orderEntity, EventType eventType, OrderStatus fromStatus, OrderStatus toStatus,
                       ActorType actorType, UUID actorId, Map<String, Object> meta) {
        this.orderEntity = orderEntity;
        this.eventType = eventType;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.actorType = actorType;
        this.actorId = actorId;
        this.eventAt = OffsetDateTime.now();
        this.meta = meta == null ? new HashMap<>() : new HashMap<>(meta); // Defensive copy to prevent external modification.
    }

    public static OrderEvent create(OrderEntity orderEntity, EventType eventType, OrderStatus fromStatus, OrderStatus toStatus,
                                    ActorType actorType, UUID actorId, Map<String, Object> meta) {
        return new OrderEvent(orderEntity, eventType, fromStatus, toStatus, actorType, actorId, meta);
    }

    public Long getEventId() {return eventId;}

    public OrderEntity getOrderEntity() {return orderEntity;}

    public EventType getEventType() {return eventType;}

    public OrderStatus getFromStatus() {return fromStatus;}

    public OrderStatus getToStatus() {return toStatus;}

    public ActorType getActorType() {return actorType;}

    public UUID getActorId() {return actorId;}

    public OffsetDateTime getEventAt() {return eventAt;}

    public Map<String, Object> getMeta() {return new HashMap<>(meta);}
}
