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
class UserBasketServiceTest {

    @Autowired
    private UserBasketService userBasketService;

    private static final UUID EMPTY_TEST_USER_UUID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID TEST_USER_UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID UNKNOWN_USER_UUID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Test
    void addBasketItem() {
        userBasketService.addItem(
                EMPTY_TEST_USER_UUID,
                1L,
                2
        );

        BasketGetResponse response =
                userBasketService.getBasket(EMPTY_TEST_USER_UUID);

        assertNotNull(response.basketId());
        assertEquals(1, response.items().size());

        var item = response.items().stream()
                .filter(i -> i.itemCode().equals(1L))
                .findFirst()
                .orElseThrow();

        assertEquals(2, item.quantity());
    }

    @Test
    void getBasket() {
        // basket 없는 정상 user
        BasketGetResponse response =
                userBasketService.getBasket(EMPTY_TEST_USER_UUID);

        assertNull(response.basketId());
        assertTrue(response.items().isEmpty());

        // basket 있는 user
        response =
                userBasketService.getBasket(TEST_USER_UUID);

        assertNotNull(response.basketId());
        assertEquals(2, response.items().size());

        var item1 = response.items().stream()
                .filter(i -> i.itemCode().equals(1L))
                .findFirst()
                .orElseThrow();

        var item2 = response.items().stream()
                .filter(i -> i.itemCode().equals(2L))
                .findFirst()
                .orElseThrow();

        assertEquals(2, item1.quantity());
        assertEquals(1, item2.quantity());

        // 존재하지 않는 user
        response =
                userBasketService.getBasket(UNKNOWN_USER_UUID);

        assertNull(response.basketId());
        assertTrue(response.items().isEmpty());
    }

    @Test
    void changeBasketQuantity() {
        userBasketService.changeQuantity(
                TEST_USER_UUID,
                1L,
                5
        );

        BasketGetResponse response =
                userBasketService.getBasket(TEST_USER_UUID);

        assertNotNull(response.basketId());
        assertEquals(2, response.items().size());

        var item1 = response.items().stream()
                .filter(i -> i.itemCode().equals(1L))
                .findFirst()
                .orElseThrow();

        var item2 = response.items().stream()
                .filter(i -> i.itemCode().equals(2L))
                .findFirst()
                .orElseThrow();

        assertEquals(5, item1.quantity());

        // 다른 basket item은 영향 없어야 함
        assertEquals(1, item2.quantity());
    }

    @Test
    void removeBasketItem() {
        userBasketService.removeItem(
                TEST_USER_UUID,
                1L
        );

        BasketGetResponse response =
                userBasketService.getBasket(TEST_USER_UUID);

        assertNotNull(response.basketId());

        // item 1만 삭제되고 item 2는 남음
        assertEquals(1, response.items().size());

        assertTrue(
                response.items().stream()
                        .noneMatch(i -> i.itemCode().equals(1L))
        );

        var remainingItem =
                response.items().stream()
                        .filter(i -> i.itemCode().equals(2L))
                        .findFirst()
                        .orElseThrow();

        assertEquals(1, remainingItem.quantity());
    }
}

//package com.seyoon.portfolio.service;
//
//import com.seyoon.portfolio.dto.response.BasketGetResponse;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class UserBasketServiceTest {
//
//    @Autowired
//    private UserBasketService userBasketService;
//
//    @Test
//    void addBasketItem() {
//        UUID userUuid =
//                UUID.fromString("33333333-3333-3333-3333-333333333333");
//
//        userBasketService.addItem(userUuid, 1L, 2);
//
//        BasketGetResponse response =
//                userBasketService.getBasket(userUuid);
//
//        assertNotNull(response.basketId());
//        assertEquals(1, response.items().size());
//        assertEquals(1L, response.items().getFirst().itemCode());
//        assertEquals(2, response.items().getFirst().quantity());
//    }
//
//    @Test
//    void getBasket() {
//        BasketGetResponse response;
//        response = userBasketService.getBasket(
//                UUID.fromString("33333333-3333-3333-3333-333333333333"));
//        assertNull(response.basketId());
//        assertTrue(response.items().isEmpty());
//        response = userBasketService.getBasket(
//                UUID.fromString("11111111-1111-1111-1111-111111111111"));
//        assertNotNull(response.basketId());
//        assertEquals(1, response.items().size());
//        assertEquals(1L, response.items().getFirst().itemCode());
//        assertEquals(2, response.items().getFirst().quantity());
//        response = userBasketService.getBasket(
//                UUID.fromString("00000000-0000-0000-0000-000000000000"));
//        assertNull(response.basketId());
//        assertTrue(response.items().isEmpty());
//    }
//
//    @Test
//    void changeBasketQuantity() {
//        userBasketService.changeQuantity(
//                UUID.fromString("11111111-1111-1111-1111-111111111111"),
//                1L,
//                5
//        );
//        BasketGetResponse response = userBasketService.getBasket(
//                UUID.fromString("11111111-1111-1111-1111-111111111111"));
//        assertNotNull(response.basketId());
//        assertEquals(1, response.items().size());
//        assertEquals(1L, response.items().getFirst().itemCode());
//        assertEquals(5, response.items().getFirst().quantity());
//    }
//
//    @Test
//    void removeBasketItem() {
//        userBasketService.removeItem(
//                UUID.fromString("11111111-1111-1111-1111-111111111111"),
//                1L
//        );
//        BasketGetResponse response = userBasketService.getBasket(UUID.fromString("11111111-1111-1111-1111-111111111111"));
//        assertNotNull(response.basketId());
//        assertTrue(response.items().isEmpty());
//    }
//}
