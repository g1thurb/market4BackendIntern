package com.seyoon.portfolio.dto.request;

import java.math.BigDecimal;

public record SellerRefundRequest(
        BigDecimal refundAmount,
        BigDecimal deductionAmount,
        String deductionReason,
        String reason
) {
}
