package com.seyoon.portfolio.dto.response;

import java.util.Map;

public record OrdersPreviewResponseAvailablePayment(Long paymentId, Map<String, Object> summary) {
}
