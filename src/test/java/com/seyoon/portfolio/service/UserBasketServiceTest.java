package com.seyoon.portfolio.service;

import com.seyoon.portfolio.dto.response.BasketGetResponse;
import com.seyoon.portfolio.support.BasketTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.seyoon.portfolio.support.BasketTestFixture.EMPTY_TEST_USER_UUID;
import static com.seyoon.portfolio.support.BasketTestFixture.TEST_USER_UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
class UserBasketServiceTest {

    @Autowired
    private UserBasketService userBasketService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final UUID UNKNOWN_USER_UUID =
            UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"
            );

    @BeforeEach
    void setUp() {
        BasketTestFixture.reset(jdbcTemplate);
    }

    @Test
    void addBasketItem() {

        userBasketService.addItem(
                EMPTY_TEST_USER_UUID,
                1L,
                2
        );

        BasketGetResponse response =
                userBasketService.getBasket(
                        EMPTY_TEST_USER_UUID
                );

        assertNotNull(response.basketId());
        assertEquals(1, response.items().size());

        var item =
                response.items().stream()
                        .filter(i ->
                                i.itemCode().equals(1L)
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(2, item.quantity());
    }

    @Test
    void getBasket() {

        // basket 없는 정상 user
        BasketGetResponse response =
                userBasketService.getBasket(
                        EMPTY_TEST_USER_UUID
                );

        assertNull(response.basketId());
        assertTrue(response.items().isEmpty());

        // basket 있는 user
        response =
                userBasketService.getBasket(
                        TEST_USER_UUID
                );

        assertNotNull(response.basketId());
        assertEquals(2, response.items().size());

        var item1 =
                response.items().stream()
                        .filter(i ->
                                i.itemCode().equals(1L)
                        )
                        .findFirst()
                        .orElseThrow();

        var item2 =
                response.items().stream()
                        .filter(i ->
                                i.itemCode().equals(2L)
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(2, item1.quantity());
        assertEquals(1, item2.quantity());

        // 존재하지 않는 user
        response =
                userBasketService.getBasket(
                        UNKNOWN_USER_UUID
                );

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
                userBasketService.getBasket(
                        TEST_USER_UUID
                );

        assertNotNull(response.basketId());
        assertEquals(2, response.items().size());

        var item1 =
                response.items().stream()
                        .filter(i ->
                                i.itemCode().equals(1L)
                        )
                        .findFirst()
                        .orElseThrow();

        var item2 =
                response.items().stream()
                        .filter(i ->
                                i.itemCode().equals(2L)
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(5, item1.quantity());

        // item2는 영향 없어야 함
        assertEquals(1, item2.quantity());
    }

    @Test
    void removeBasketItem() {

        userBasketService.removeItem(
                TEST_USER_UUID,
                1L
        );

        BasketGetResponse response =
                userBasketService.getBasket(
                        TEST_USER_UUID
                );

        assertNotNull(response.basketId());
        assertEquals(1, response.items().size());

        assertTrue(
                response.items().stream()
                        .noneMatch(i ->
                                i.itemCode().equals(1L)
                        )
        );

        var remainingItem =
                response.items().stream()
                        .filter(i ->
                                i.itemCode().equals(2L)
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                1,
                remainingItem.quantity()
        );
    }
}