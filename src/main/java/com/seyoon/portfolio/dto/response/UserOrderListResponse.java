package com.seyoon.portfolio.dto.response;

import java.util.List;

public record UserOrderListResponse(
        List<UserOrderListResponseItem> userOrderListResponseItems,
        int pageNumber,
        int pageSize, // => int pageSize = 30
        int totalPages,
        long totalElements,
        boolean hasNext,
        boolean hasPrevious
        ) {
}
