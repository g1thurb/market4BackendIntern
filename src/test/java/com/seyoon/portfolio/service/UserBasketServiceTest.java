package com.seyoon.portfolio.service;

import com.seyoon.portfolio.dto.response.BasketGetResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserBasketServiceTest {

    @Autowired
    private UserBasketService userBasketService;

    @Test
    void addBasketItem() {
        UUID userUuid =
                UUID.fromString("33333333-3333-3333-3333-333333333333");

        userBasketService.addItem(userUuid, 1L, 2);

        BasketGetResponse response =
                userBasketService.getBasket(userUuid);

        assertNotNull(response.basketId());
        assertEquals(1, response.items().size());
        assertEquals(1L, response.items().getFirst().itemCode());
        assertEquals(2, response.items().getFirst().quantity());
    }

    @Test
    void getBasket() {
        BasketGetResponse response;
        response = userBasketService.getBasket(
                UUID.fromString("33333333-3333-3333-3333-333333333333"));
        assertNull(response.basketId());
        assertTrue(response.items().isEmpty());
        response = userBasketService.getBasket(
                UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertNotNull(response.basketId());
        assertEquals(1, response.items().size());
        assertEquals(1L, response.items().getFirst().itemCode());
        assertEquals(2, response.items().getFirst().quantity());
        response = userBasketService.getBasket(
                UUID.fromString("00000000-0000-0000-0000-000000000000"));
        assertNull(response.basketId());
        assertTrue(response.items().isEmpty());
    }

    @Test
    void changeBasketQuantity() {
        userBasketService.changeQuantity(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                1L,
                5
        );
        BasketGetResponse response = userBasketService.getBasket(
                UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertNotNull(response.basketId());
        assertEquals(1, response.items().size());
        assertEquals(1L, response.items().getFirst().itemCode());
        assertEquals(5, response.items().getFirst().quantity());
    }

    @Test
    void removeBasketItem() {
        userBasketService.removeItem(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                1L
        );
        BasketGetResponse response = userBasketService.getBasket(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertNotNull(response.basketId());
        assertTrue(response.items().isEmpty());
    }
}
