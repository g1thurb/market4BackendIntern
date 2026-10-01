package com.seyoon.portfolio.service;

import com.seyoon.portfolio.dto.PurchaseType;
import com.seyoon.portfolio.exception.InsufficientStockException;
import com.seyoon.portfolio.repository.BasketItemRepository;
import com.seyoon.portfolio.repository.ItemRepository;
import com.seyoon.portfolio.repository.UserBasketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserOrderTransactionTest {

    @Autowired
    private UserOrderService userOrderService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserBasketRepository userBasketRepository;

    @Autowired
    private BasketItemRepository basketItemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final UUID TEST_USER_UUID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final Long TEST_ADDRESS_ID = 3L;
    private static final Long TEST_PAYMENT_ID = 1L;

    private static final Long ITEM_1_ID = 1L;
    private static final Long ITEM_2_ID = 2L;

    @Test
    void basketPartialStockFailure_rollsBackEntireOrder() {
        // given
        int originalItem1Stock =
                itemRepository.findById(ITEM_1_ID)
                        .orElseThrow()
                        .getAvailable();

        int originalItem2Stock =
                itemRepository.findById(ITEM_2_ID)
                        .orElseThrow()
                        .getAvailable();

        Long basketId =
                userBasketRepository
                        .findByUserInfo_Uuid(TEST_USER_UUID)
                        .orElseThrow()
                        .getBasketId();

        int basketItemCountBefore =
                basketItemRepository
                        .findAllByUserBasket_BasketId(basketId)
                        .size();

        try {
            /*
             * itemCode 순서:
             *
             * item 1 -> stock 감소 성공
             * item 2 -> stock 감소 실패
             *
             * 따라서 item 1 감소까지 전체 rollback되는지 확인한다.
             */
            jdbcTemplate.update(
                    "UPDATE items SET available = 10 WHERE item_code = ?",
                    ITEM_1_ID
            );

            jdbcTemplate.update(
                    "UPDATE items SET available = 0 WHERE item_code = ?",
                    ITEM_2_ID
            );

            // when
            assertThrows(
                    InsufficientStockException.class,
                    () -> userOrderService.newOrderBasket(
                            TEST_USER_UUID,
                            TEST_ADDRESS_ID,
                            List.of(),
                            PurchaseType.CARD,
                            TEST_PAYMENT_ID
                    )
            );

            // then

            // item1은 먼저 -2 됐어야 하지만,
            // transaction rollback으로 다시 10이어야 한다.
            int item1After =
                    itemRepository.findById(ITEM_1_ID)
                            .orElseThrow()
                            .getAvailable();

            int item2After =
                    itemRepository.findById(ITEM_2_ID)
                            .orElseThrow()
                            .getAvailable();

            assertEquals(10, item1After);
            assertEquals(0, item2After);

            // basket도 삭제되지 않아야 한다.
            assertTrue(
                    userBasketRepository
                            .findByUserInfo_Uuid(TEST_USER_UUID)
                            .isPresent()
            );

            assertEquals(
                    basketItemCountBefore,
                    basketItemRepository
                            .findAllByUserBasket_BasketId(basketId)
                            .size()
            );

        } finally {
            // personal_db fixture 복구
            jdbcTemplate.update(
                    "UPDATE items SET available = ? WHERE item_code = ?",
                    originalItem1Stock,
                    ITEM_1_ID
            );

            jdbcTemplate.update(
                    "UPDATE items SET available = ? WHERE item_code = ?",
                    originalItem2Stock,
                    ITEM_2_ID
            );
        }
    }
}