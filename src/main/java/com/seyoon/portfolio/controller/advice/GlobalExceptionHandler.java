package com.seyoon.portfolio.controller.advice;

import com.seyoon.portfolio.dto.response.BasketErrorResponse;
import com.seyoon.portfolio.dto.response.OrderErrorResponse;
import com.seyoon.portfolio.exception.CouponNotFoundException;
import com.seyoon.portfolio.exception.InsufficientStockException;
import com.seyoon.portfolio.exception.InvalidQuantityException;
import com.seyoon.portfolio.exception.ItemOutOfStockException;
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
}