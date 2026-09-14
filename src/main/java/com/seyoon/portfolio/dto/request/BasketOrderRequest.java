package com.seyoon.portfolio.dto.request;

import com.seyoon.portfolio.dto.PurchaseType;

import java.util.List;

public record BasketOrderRequest(List<String> couponCodes, Long addressId, PurchaseType purchaseType, Long paymentId) {
}
