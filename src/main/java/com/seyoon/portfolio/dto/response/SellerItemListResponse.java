package com.seyoon.portfolio.dto.response;

import java.util.List;

public record SellerItemListResponse(
        List<SellerItemListResponseItem> sellerItemListResponseItems,
        int pageNumber,
        int pageSize,
        int totalPages,
        long totalElements,
        boolean hasNext,
        boolean hasPrevious
) {
}
