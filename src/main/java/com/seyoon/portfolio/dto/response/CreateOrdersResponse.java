package com.seyoon.portfolio.dto.response;

import java.util.List;

public record CreateOrdersResponse(Long checkoutId, List<Long> orderIds) {
}
//    {
//        "checkoutId" :,
//        "orderIds" :
//    }//잘 끝나면 orders로 이동