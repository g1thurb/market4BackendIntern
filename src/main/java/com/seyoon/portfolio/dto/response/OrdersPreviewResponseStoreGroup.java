package com.seyoon.portfolio.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrdersPreviewResponseStoreGroup(
        UUID storeUuid,
        String storeName,
        BigDecimal orderSubtotal,
        BigDecimal deliveryFee,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        List<OrdersPreviewResponseItemGroup> items
) {}
//    "storeGroups": [
//            {
//            "storeUuid": ,
//            "storeName": ,
//            "orderSubtotal": ,
//            "deliveryFee": ,
//            "discountAmount": ,
//            "finalAmount": ,
//            "items": [
//            {
//            "itemCode": ,
//            "itemName": ,
//            "quantity": ,
//            "unitPrice": ,
//            "lineTotal": ,
//            "mainImage":
//            }
//            ]
//        }
//    ],