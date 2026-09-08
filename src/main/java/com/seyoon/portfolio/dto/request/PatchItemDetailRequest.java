package com.seyoon.portfolio.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record PatchItemDetailRequest(
        String itemName,
        BigDecimal price,
        String deliveryType, //TODO 추후 enum type 만들지 고민
        List<String> mainImages, //String mainImages,
        String describeText,
        Integer available
) {
}
