package com.seyoon.portfolio.dto.request;

public record SellerProfilePatchRequest(
        String storeName,
        String storeTin,
        String storeCrn,
        String storeBrn
) {
}
