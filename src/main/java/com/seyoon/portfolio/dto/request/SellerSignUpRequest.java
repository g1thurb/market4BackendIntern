package com.seyoon.portfolio.dto.request;

public record SellerSignUpRequest(
        String id,
        String password,
        String passwordConfirm,
        String storeName,
        String storeTin,
        String storeCrn,
        String storeBrn
) {
}
// generatedDate는 request로 받지 않고 서버가 생성한다.