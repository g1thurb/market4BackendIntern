package com.seyoon.portfolio.dto.view.request;

import com.seyoon.portfolio.entity.type.DeliveredMarkedBy;

import java.time.OffsetDateTime;

public record UserOrderDetailViewDelivery(
        Long courierCode,
        Long trackingNumber,
        OffsetDateTime deliveredDate,
        DeliveredMarkedBy deliveredMarkedBy
) {
}
