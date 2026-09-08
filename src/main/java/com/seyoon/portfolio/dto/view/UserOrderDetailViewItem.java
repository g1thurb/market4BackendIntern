package com.seyoon.portfolio.dto.view.request;

import java.math.BigDecimal;

public record UserOrderDetailViewItem(
        Long orderItemId,
        Long itemCode,
        String itemName,
        String mainImage,
        int quantity,
        BigDecimal unitPriceAtPurchase,
        BigDecimal lineTotal
) {
}
