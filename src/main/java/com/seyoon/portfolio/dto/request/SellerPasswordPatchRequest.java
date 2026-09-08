package com.seyoon.portfolio.dto.request;

public record SellerPasswordPatchRequest(
        String currentPassword,
        String newPassword
) {
}
