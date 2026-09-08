package com.seyoon.portfolio.dto.request;

public record SellerLoginRequest(
        String id,
        String password
        //TODO password hashing apply
) {
}
