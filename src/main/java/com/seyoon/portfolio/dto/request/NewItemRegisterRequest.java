package com.seyoon.portfolio.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record NewItemRegisterRequest(
        String itemName,
        BigDecimal price,
        String deliveryType, // dataType
        List<String> mainImages, //String mainImages,
        String describeText,
        int available
) {
}
