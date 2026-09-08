package com.seyoon.portfolio.dto.response;

import com.seyoon.portfolio.entity.type.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record SellerOrderListResponseItem(
        Long orderId,
        Long checkoutId,
        OrderStatus status,
        OffsetDateTime orderedAt,
        String representativeItemName,
        String thumbnailUrl,
        int quantitySummary,
        BigDecimal totalAmount,
        List<String> availableActions
) {
}
