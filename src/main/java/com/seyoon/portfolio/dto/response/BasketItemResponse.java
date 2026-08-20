package com.seyoon.portfolio.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record BasketItemResponse(Long basketItemId, Long itemCode, UUID storeUuid, String storeName, String itemName, String mainImage,
                                 BigDecimal currentPrice, int quantity, int available, BigDecimal lineTotal, boolean sellable) {
}