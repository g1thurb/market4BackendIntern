package com.seyoon.portfolio.dto.response;

public record AvailabilityResponse(
        String candidate, // dataType 헷갈림
        boolean available
) {
}
