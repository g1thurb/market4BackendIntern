package com.seyoon.portfolio.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record OrdersPreviewResponse(
        BigDecimal checkoutTotalAmount,
        List<OrdersPreviewResponseStoreGroup> storeGroups,
        List<OrdersPreviewResponseAvailableAddress> availableAddresses,
        List<OrdersPreviewResponseAvailablePayment> availablePayments,
        List<String> warnings
) {}

//    {
//    "checkoutTotalAmount": ,
//    "storeGroups": [
//      {
//        "storeUuid": ,
//        "storeName": ,
//        "orderSubtotal": ,
//        "deliveryFee": ,
//        "discountAmount": ,
//        "finalAmount": ,
//        "items": [
//          {
//            "itemCode": ,
//            "itemName": ,
//            "quantity": ,
//            "unitPrice": ,
//            "lineTotal": ,
//            "mainImage":
//          }
//        ]
//      }
//    ],
//    "availableAddresses": [ { "addressId": , "address": } ],
//    "availablePayments": [ { "paymentId": , "summary": } ],
//    "warnings": []
//  }