package com.seyoon.portfolio.dto.request;

public record ItemOrderRequest(Long itemCode, int quantity, String couponCode) {
}
