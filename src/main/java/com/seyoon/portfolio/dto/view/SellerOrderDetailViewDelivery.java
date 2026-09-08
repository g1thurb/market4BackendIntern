package com.seyoon.portfolio.dto.view;

import java.time.OffsetDateTime;

public record SellerOrderDetailViewDelivery(
        Long courierCode,
        Long trackingNumber,
        OffsetDateTime deliveredDate,
        String deliveredMarkedBy
) {
}
