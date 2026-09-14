package com.seyoon.portfolio.controller.api;

import com.seyoon.portfolio.dto.request.BasketOrderPreviewRequest;
import com.seyoon.portfolio.dto.request.BasketOrderRequest;
import com.seyoon.portfolio.dto.request.ItemOrderPreviewRequest;
import com.seyoon.portfolio.dto.request.ItemOrderRequest;
import com.seyoon.portfolio.dto.response.CreateOrdersResponse;
import com.seyoon.portfolio.dto.response.OrdersPreviewResponse;
import com.seyoon.portfolio.service.UserOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/orders")
public class UserOrderApiController {

    public final UserOrderService userOrderService;

    public UserOrderApiController(UserOrderService userOrderService) {
        this.userOrderService = userOrderService;
    }

    //@RequestParam UUID userUuid은 보언인증파트 끝나면 @RequestParam을 없애기 + 추가할 것 있으면 하기
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/preview/basket")
    public OrdersPreviewResponse getUserBasketOrdersPreview(@RequestParam UUID userUuid,
                                                            @RequestBody BasketOrderPreviewRequest basketOrderPreviewRequest) {
        return userOrderService.newPreviewBasket(userUuid, basketOrderPreviewRequest.couponCodes());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/preview/item")
    public OrdersPreviewResponse getUserItemOrdersPreview(
            @RequestParam UUID userUuid, @RequestBody ItemOrderPreviewRequest request) {
        return userOrderService.newPreviewItem(userUuid, request.itemCode(), request.quantity(), request.couponCode());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/basket")
    public CreateOrdersResponse getUserBasketOrders(@RequestParam UUID userUuid, @RequestBody BasketOrderRequest basketOrderRequest) {
        return userOrderService.newOrderBasket(userUuid,basketOrderRequest.addressId(),
                basketOrderRequest.couponCodes(), basketOrderRequest.purchaseType(),  basketOrderRequest.paymentId());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/item")
    public CreateOrdersResponse getUserItemOrders(@RequestParam UUID userUuid, @RequestBody ItemOrderRequest itemOrderRequest) {
        return userOrderService.newOrderItem(userUuid,
                itemOrderRequest.itemCode(), itemOrderRequest.quantity(), itemOrderRequest.couponCode(),
                itemOrderRequest.addressId(), itemOrderRequest.purchaseType(), itemOrderRequest.paymentId());
    }
}
