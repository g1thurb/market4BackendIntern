package com.seyoon.portfolio.dto.view;

import java.math.BigDecimal;

public record SellerOrderDetailViewItem(
        Long orderItemId,
        Long itemCode,
        String itemName,
        String mainImage,
        int quantity,
        BigDecimal unitPriceAtPurchase,
        BigDecimal lineTotal
) {
}
