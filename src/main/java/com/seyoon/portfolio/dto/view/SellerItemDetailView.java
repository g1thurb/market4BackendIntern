package com.seyoon.portfolio.dto.view;

import java.math.BigDecimal;
import java.util.List;

public record SellerItemDetailView(
        long itemCode,
        String itemName,
        BigDecimal price,
        String deliveryType, // TODO dataType 재정의
        List<String> mainImages, //String mainImages,
        String describeText,
        int available
) {
}
