package com.seyoon.portfolio.dto.request;

public record ShipmentRequest(
        String courierCode, // TODO 필요하면 dataType 바꾸기
        String trackingNumber // TODO 필요하면 dataType 바꾸기
) {
}
