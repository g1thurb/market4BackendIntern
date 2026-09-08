package com.seyoon.portfolio.dto.view;

import com.seyoon.portfolio.entity.Payment;
import com.seyoon.portfolio.entity.Refund;
import com.seyoon.portfolio.entity.UserInfo;
import com.seyoon.portfolio.entity.type.CheckoutStatus;
import com.seyoon.portfolio.entity.type.ConfirmedBy;
import com.seyoon.portfolio.entity.type.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record SellerOrderDetailView(
        Long checkoutId,
        Long orderId,
        CheckoutStatus checkoutStatus,
        // UserInfo userInfo, → SellerOrderDetailUserView
        String storeName,
        OrderStatus status,
        OffsetDateTime orderedAt,
        List<SellerOrderDetailViewItem> sellerOrderDetailViewItems,
        // Payment payment, → PaymentView
        // List<Refund> refunds, → List<RefundView>
        SellerOrderDetailViewDelivery sellerOrderDetailViewDelivery,
        OffsetDateTime confirmDeadlineDate,
        boolean confirmExtended,
        OffsetDateTime confirmDate,
        ConfirmedBy confirmedBy,
        List<String> availableActions
) {
}
