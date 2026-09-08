package com.seyoon.portfolio.dto.view;

import com.seyoon.portfolio.entity.Coupon;
import com.seyoon.portfolio.entity.Payment;
import com.seyoon.portfolio.entity.Refund;
import com.seyoon.portfolio.entity.type.CheckoutStatus;
import com.seyoon.portfolio.entity.type.ConfirmedBy;
import com.seyoon.portfolio.entity.type.OrderStatus;
import com.seyoon.portfolio.dto.view.request.UserOrderDetailViewItem;
import com.seyoon.portfolio.dto.view.request.UserOrderDetailViewDelivery;

import java.time.OffsetDateTime;
import java.util.List;

public record UserOrderDetailView(
        Long checkoutId,
        Long orderId,
        CheckoutStatus checkoutStatus,
        String storeName,
        OrderStatus status,
        OffsetDateTime orderedAt,
        List<UserOrderDetailViewItem> userOrderDetailViewItems,
        // Coupon coupon,
        // Payment payment,
        // List<Refund> refunds,
        UserOrderDetailViewDelivery userOrderDetailViewDelivery,
        OffsetDateTime confirmDeadlineDate,
        Boolean confirmExtended,
        OffsetDateTime confirmDate,
        ConfirmedBy confirmedBy,
        List<String> availableActions // find proper element datatype later
) {
}
