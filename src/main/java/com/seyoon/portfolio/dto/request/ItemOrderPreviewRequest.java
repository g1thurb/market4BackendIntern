package com.seyoon.portfolio.dto.request;

public record ItemOrderPreviewRequest(Long itemCode, int quantity, String couponCode) {
}
