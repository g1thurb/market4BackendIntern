package com.seyoon.portfolio.dto.response;

import java.util.Date;

public record SellerProfileGetResponse(
        String storeName,
        String storeTin,
        String storeCrn,
        String storeBrn,
        Date generatedDate
) {
}
