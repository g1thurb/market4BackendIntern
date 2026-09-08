package com.seyoon.portfolio.dto.request;

public record SellerResetPasswordRequest(
        String id,
        String storeName,
        String storeTin,
        String storeCrn,
        String storeBrn
) {
}
