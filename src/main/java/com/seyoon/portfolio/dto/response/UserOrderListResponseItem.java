package com.seyoon.portfolio.dto.response;

import com.seyoon.portfolio.entity.type.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UserOrderListResponseItem(
        Long orderId,
        Long checkoutId,
        OrderStatus orderStatus,
        OffsetDateTime orderedAt,
        String storeName,
        String representativeItemName,
        String thumbnailUrl,
        BigDecimal totalAmount
) {
}
