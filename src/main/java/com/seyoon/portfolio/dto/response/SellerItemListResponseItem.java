package com.seyoon.portfolio.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SellerItemListResponseItem(
        Long itemCode,
        String itemName,
        BigDecimal price,
        int available,
        String mainImage,
        OffsetDateTime uploadDate
) {
}
