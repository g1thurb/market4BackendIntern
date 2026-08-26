package com.seyoon.portfolio.dto.response.trashbin;

import com.seyoon.portfolio.dto.response.OrdersPreviewResponseAvailableAddress;
import com.seyoon.portfolio.dto.response.OrdersPreviewResponseAvailablePayment;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ItemOrdersPreviewResponse(
        Long itemCode,
        String itemName,
        UUID storeUuid,
        String storeName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal itemTotal,
        BigDecimal deliveryFee,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        List<OrdersPreviewResponseAvailableAddress> availableAddresses,
        List<OrdersPreviewResponseAvailablePayment> availablePayments,
        List<String> warnings
) {
}
//	{
//    "itemCode": ,
//    "itemName": ,
//    "storeUuid": ,
//    "storeName": ,
//    "quantity": ,
//    "unitPrice": ,
//    "itemTotal": ,
//    "deliveryFee": ,
//    "discountAmount": ,
//    "finalAmount": ,
//    "availableAddresses": [ { "addressId": , "address": } ],
//    "availablePayments": [ { "paymentId": , "summary": } ],
//    "warnings": []
//  }