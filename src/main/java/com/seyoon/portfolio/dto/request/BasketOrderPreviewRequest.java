package com.seyoon.portfolio.dto.request;

import java.util.List;

public record BasketOrderPreviewRequest(List<String> couponCodes) {
}// 나중에 List<String> couponCodes 쓰기;
