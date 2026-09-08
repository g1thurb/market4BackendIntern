package com.seyoon.portfolio.dto.response;

import java.util.List;

public record SellerOrderListResponse(
        List<SellerOrderListResponseItem> sellerOrderListResponseItems,
        int pageNumber,
        int pageSize,
        int totalPages,
        long totalElements,
        boolean hasNext,
        boolean hasPrevious
) {
}
