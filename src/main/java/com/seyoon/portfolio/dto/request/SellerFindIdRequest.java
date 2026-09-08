package com.seyoon.portfolio.dto.request;

public record SellerFindIdRequest(
        String storeName,
        String storeTin,
        String storeCrn,
        String storeBrn
) {
}
