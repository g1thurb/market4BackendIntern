package com.seyoon.portfolio.dto.request;

import com.seyoon.portfolio.dto.PurchaseType;

public record ItemOrderRequest(Long itemCode, int quantity, String couponCode, PurchaseType purchaseType, Long paymentId) {
}
