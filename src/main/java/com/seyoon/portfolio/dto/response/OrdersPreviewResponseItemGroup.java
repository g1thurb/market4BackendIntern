package com.seyoon.portfolio.dto.response;

import java.math.BigDecimal;

public record OrdersPreviewResponseItemGroup(
        Long itemCode,
        String itemName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        String mainImage
) {}