package com.seyoon.portfolio.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record BasketGetResponse(Long basketId,
                                List<BasketItemResponse> items,
                                BigDecimal totalAmount) {}
//BasketGetResponse
//{
//    "basketId": Long,
//    "items": List<BasketItemResponse>[
//    {
//        "basketItemId": ,
//        "itemCode": ,
//        "storeUuid": ,
//        "storeName": ,
//        "itemName": ,
//        "mainImage": ,
//        "currentPrice": ,
//        "quantity": ,
//        "available": ,
//        "lineTotal": ,
//        "sellable":
//    }, ...
//    ],
//    "totalAmount":
//}