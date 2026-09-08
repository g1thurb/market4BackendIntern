package com.seyoon.portfolio.controller.advice;

import com.seyoon.portfolio.dto.response.BasketErrorResponse;
import com.seyoon.portfolio.dto.response.OrderErrorResponse;
import com.seyoon.portfolio.exception.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidQuantityException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BasketErrorResponse handleInvalidQuantity(
            InvalidQuantityException exception
    ) {
        return new BasketErrorResponse(
                "INVALID_QUANTITY",
                exception.getMessage()
        );
    }

    @ExceptionHandler(ItemOutOfStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public BasketErrorResponse handleItemOutOfStock(
            ItemOutOfStockException exception
    ) {
        return new BasketErrorResponse(
                "ITEM_OUT_OF_STOCK",
                exception.getMessage()
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public BasketErrorResponse handleInsufficientStock(
            InsufficientStockException exception
    ) {
        return new BasketErrorResponse(
                "INSUFFICIENT_STOCK",
                exception.getMessage()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BasketErrorResponse handleEntityNotFound(
            EntityNotFoundException exception
    ) {
        return new BasketErrorResponse(
                "NOT_FOUND",
                exception.getMessage()
        );
    }

    @ExceptionHandler(CouponNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public OrderErrorResponse couponNotFound(
            CouponNotFoundException exception
    ) {
        return new OrderErrorResponse(
                "COUPON_NOT_FOUND",
                exception.getMessage()
        );
    }

    @ExceptionHandler(CouponNotMatchException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public OrderErrorResponse couponNotMatch(
            CouponNotMatchException exception
    ) {
        return new OrderErrorResponse(
                "COUPON_NOT_MATCH",
                exception.getMessage()
        );
    }

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public OrderErrorResponse itemNotFound(
            ItemNotFoundException exception
    ) {
        return new OrderErrorResponse(
                "ITEM_NOT_FOUND",
                exception.getMessage()
        );
    }

    @ExceptionHandler(PurchaseFailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public OrderErrorResponse purchaseFail(
            PurchaseFailException exception
    ) {
        return new OrderErrorResponse(
                "PURCHASE_FAILED",
                exception.getMessage()
        );
    }
}


//TODO
//한 가지 구조적으로는
//InvalidQuantityException, ItemOutOfStockException, InsufficientStockException이
//Basket에서도 Order에서도 사용되는데 BasketErrorResponse를 반환하고 있잖아.
//public BasketErrorResponse handleInsufficientStock(...)
//기능상 아무 문제는 없어. JSON 구조도 api + message뿐이니까 잘 동작해.
//다만 나중에 리팩터링할 때:
//BasketErrorResponse, OrderErrorResponse
//둘 다 필드가 똑같다면 그냥:
//ApiErrorResponse(String api, String message)
//하나로 합칠 수 있음