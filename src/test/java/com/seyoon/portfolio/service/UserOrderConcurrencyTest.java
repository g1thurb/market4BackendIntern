package com.seyoon.portfolio.service;

import com.seyoon.portfolio.dto.PurchaseType;
import com.seyoon.portfolio.dto.response.CreateOrdersResponse;
import com.seyoon.portfolio.exception.InsufficientStockException;
import com.seyoon.portfolio.repository.CheckoutRepository;
import com.seyoon.portfolio.repository.ItemRepository;
import com.seyoon.portfolio.repository.OrderItemRepository;
import com.seyoon.portfolio.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserOrderConcurrencyTest {

    @Autowired
    private UserOrderService userOrderService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final UUID TEST_USER_UUID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final Long TEST_ITEM_ID = 1L;
    private static final Long TEST_ADDRESS_ID = 3L;
    private static final Long TEST_PAYMENT_ID = 1L;

    @Test
    void lastItemConcurrentPurchase_exactlyOneSucceeds()
            throws Exception {

        // given
        int originalStock =
                itemRepository.findById(TEST_ITEM_ID)
                        .orElseThrow()
                        .getAvailable();

        jdbcTemplate.update(
                "UPDATE items SET available = 1 WHERE item_code = ?",
                TEST_ITEM_ID
        );

        int threadCount = 2;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        /*
         * 두 thread 모두 준비됐는지 확인
         */
        CountDownLatch readyLatch =
                new CountDownLatch(threadCount);

        /*
         * 두 thread를 동시에 출발시키기 위한 latch
         */
        CountDownLatch startLatch =
                new CountDownLatch(1);

        AtomicInteger successCount =
                new AtomicInteger();

        AtomicInteger insufficientStockCount =
                new AtomicInteger();

        /*
         * cleanup을 위해 성공한 주문들을 보관한다.
         *
         * ConcurrentLinkedQueue는 여러 thread가 동시에 add해도 안전하다.
         */
        ConcurrentLinkedQueue<CreateOrdersResponse>
                successfulResponses =
                new ConcurrentLinkedQueue<>();

        List<Future<Void>> futures =
                new ArrayList<>();

        try {
            for (int i = 0; i < threadCount; i++) {

                Future<Void> future =
                        executor.submit(() -> {

                            readyLatch.countDown();

                            /*
                             * main thread가 startLatch를 열 때까지
                             * 두 주문 모두 대기
                             */
                            startLatch.await();

                            try {
                                CreateOrdersResponse response =
                                        userOrderService.newOrderItem(
                                                TEST_USER_UUID,
                                                TEST_ITEM_ID,
                                                1,
                                                null,
                                                TEST_ADDRESS_ID,
                                                PurchaseType.CARD,
                                                TEST_PAYMENT_ID
                                        );

                                successfulResponses.add(response);
                                successCount.incrementAndGet();

                            } catch (InsufficientStockException e) {

                                insufficientStockCount
                                        .incrementAndGet();
                            }

                            return null;
                        });

                futures.add(future);
            }

            /*
             * 두 thread가 모두 출발선까지 온 것을 확인한다.
             */
            assertTrue(
                    readyLatch.await(
                            5,
                            TimeUnit.SECONDS
                    )
            );

            // when
            startLatch.countDown();

            /*
             * 두 주문이 모두 끝날 때까지 기다린다.
             *
             * unexpected exception이 발생하면
             * future.get()에서 테스트가 실패한다.
             */
            for (Future<Void> future : futures) {
                future.get(
                        10,
                        TimeUnit.SECONDS
                );
            }

            // then
            assertEquals(
                    1,
                    successCount.get()
            );

            assertEquals(
                    1,
                    insufficientStockCount.get()
            );

            int finalStock =
                    itemRepository.findById(TEST_ITEM_ID)
                            .orElseThrow()
                            .getAvailable();

            assertEquals(
                    0,
                    finalStock
            );

        } finally {
            /*
             * 혹시 ready 단계에서 테스트가 실패하더라도
             * 기다리는 thread를 풀어준다.
             */
            startLatch.countDown();

            executor.shutdownNow();

            try {
                executor.awaitTermination(
                        5,
                        TimeUnit.SECONDS
                );
            } finally {
                /*
                 * 이 테스트는 실제 transaction을 commit해야
                 * concurrency를 검증할 수 있다.
                 *
                 * 따라서 성공한 주문을 직접 삭제한다.
                 */
                cleanupSuccessfulOrders(
                        successfulResponses
                );

                // stock fixture 원복
                jdbcTemplate.update(
                        "UPDATE items SET available = ? WHERE item_code = ?",
                        originalStock,
                        TEST_ITEM_ID
                );
            }
        }
    }

    private void cleanupSuccessfulOrders(
            Iterable<CreateOrdersResponse> responses
    ) {
        for (CreateOrdersResponse response : responses) {

            for (Long orderId : response.orderIds()) {

                var orderItems =
                        orderItemRepository
                                .findAllByOrderEntity_OrderId(
                                        orderId
                                );

                orderItemRepository.deleteAll(
                        orderItems
                );

                orderRepository
                        .findById(orderId)
                        .ifPresent(
                                orderRepository::delete
                        );
            }

            checkoutRepository
                    .findById(response.checkoutId())
                    .ifPresent(
                            checkoutRepository::delete
                    );
        }
    }
}