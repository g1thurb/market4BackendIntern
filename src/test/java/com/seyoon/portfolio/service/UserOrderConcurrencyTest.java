package com.seyoon.portfolio.service;

import com.seyoon.portfolio.dto.PurchaseType;
import com.seyoon.portfolio.dto.response.CreateOrdersResponse;
import com.seyoon.portfolio.exception.InsufficientStockException;
import com.seyoon.portfolio.repository.CheckoutRepository;
import com.seyoon.portfolio.repository.ItemRepository;
import com.seyoon.portfolio.repository.OrderItemRepository;
import com.seyoon.portfolio.repository.OrderRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * show-sql은 측정 시간에 stdout 출력 비용이 섞이지 않도록 이 테스트에서만 끈다.
 */
@SpringBootTest(properties = "spring.jpa.show-sql=false")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserOrderConcurrencyTest {

    private static final Logger log =
            LoggerFactory.getLogger(UserOrderConcurrencyTest.class);

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

    @Autowired
    private DataSource dataSource;

    private static final UUID TEST_USER_UUID =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final Long TEST_ITEM_ID = 1L;
    private static final Long TEST_ADDRESS_ID = 3L;
    private static final Long TEST_PAYMENT_ID = 1L;

    /*
     * 마지막 재고 1개를 threadCount명이 동시에 구매한다.
     * 정확히 1건만 성공해야 한다.
     */
    @Order(1)
    @ParameterizedTest(name = "stock=1, threads={0}")
    @ValueSource(ints = {2, 10, 50, 100})
    void lastItemConcurrentPurchase_exactlyOneSucceeds(int threadCount)
            throws Exception {

        runConcurrentPurchase(1, threadCount);
    }

    /*
     * 재고 N개를 그보다 많은 thread가 동시에 구매한다.
     * 정확히 N건만 성공해야 한다.
     */
    @Order(2)
    @Test
    void limitedStockConcurrentPurchase_exactlyStockSucceeds()
            throws Exception {

        runConcurrentPurchase(10, 100);
    }

    private void runConcurrentPurchase(int initialStock, int threadCount)
            throws Exception {

        // given
        int originalStock =
                itemRepository.findById(TEST_ITEM_ID)
                        .orElseThrow()
                        .getAvailable();

        jdbcTemplate.update(
                "UPDATE items SET available = ? WHERE item_code = ?",
                initialStock,
                TEST_ITEM_ID
        );

        long ordersBefore = countOrders();
        long orderItemsBefore = countOrderItems();

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        /*
         * 모든 thread가 준비됐는지 확인
         */
        CountDownLatch readyLatch =
                new CountDownLatch(threadCount);

        /*
         * 모든 thread를 동시에 출발시키기 위한 latch
         */
        CountDownLatch startLatch =
                new CountDownLatch(1);

        AtomicInteger successCount =
                new AtomicInteger();

        AtomicInteger insufficientStockCount =
                new AtomicInteger();

        /*
         * InsufficientStockException 이외의 예외.
         * (예: 커넥션 풀 대기 timeout, deadlock 등)
         */
        ConcurrentLinkedQueue<Throwable> unexpectedExceptions =
                new ConcurrentLinkedQueue<>();

        /*
         * thread별 요청 소요 시간 (startLatch 통과 ~ service 반환)
         * 커넥션 대기 시간이 여기에 포함된다.
         */
        long[] requestNanos = new long[threadCount];

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

                final int index = i;

                Future<Void> future =
                        executor.submit(() -> {

                            readyLatch.countDown();

                            /*
                             * main thread가 startLatch를 열 때까지
                             * 모든 주문 대기
                             */
                            startLatch.await();

                            long begin = System.nanoTime();

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

                            } catch (Throwable t) {

                                unexpectedExceptions.add(t);

                            } finally {
                                requestNanos[index] =
                                        System.nanoTime() - begin;
                            }

                            return null;
                        });

                futures.add(future);
            }

            /*
             * 모든 thread가 출발선까지 온 것을 확인한다.
             */
            assertTrue(
                    readyLatch.await(
                            10,
                            TimeUnit.SECONDS
                    )
            );

            // when
            long startedAt = System.nanoTime();
            startLatch.countDown();

            /*
             * 모든 주문이 끝날 때까지 기다린다.
             */
            for (Future<Void> future : futures) {
                future.get(
                        60,
                        TimeUnit.SECONDS
                );
            }

            long elapsedNanos = System.nanoTime() - startedAt;

            int finalStock =
                    itemRepository.findById(TEST_ITEM_ID)
                            .orElseThrow()
                            .getAvailable();

            long ordersCreated = countOrders() - ordersBefore;
            long orderItemsCreated = countOrderItems() - orderItemsBefore;

            logResult(
                    initialStock,
                    threadCount,
                    successCount.get(),
                    insufficientStockCount.get(),
                    unexpectedExceptions.size(),
                    finalStock,
                    ordersCreated,
                    orderItemsCreated,
                    elapsedNanos,
                    requestNanos
            );

            // then
            assertTrue(
                    unexpectedExceptions.isEmpty(),
                    () -> "unexpected exceptions: " + unexpectedExceptions
            );

            assertEquals(
                    initialStock,
                    successCount.get()
            );

            assertEquals(
                    threadCount - initialStock,
                    insufficientStockCount.get()
            );

            assertEquals(
                    0,
                    finalStock
            );

            assertEquals(
                    successCount.get(),
                    ordersCreated
            );

            assertEquals(
                    successCount.get(),
                    orderItemsCreated
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
                        10,
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

    private long countOrders() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM orders WHERE user_uuid = ?",
                Long.class,
                TEST_USER_UUID
        );
        return count == null ? 0 : count;
    }

    private long countOrderItems() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM order_items WHERE item_code = ?",
                Long.class,
                TEST_ITEM_ID
        );
        return count == null ? 0 : count;
    }

    private void logResult(
            int initialStock,
            int threadCount,
            int success,
            int rejected,
            int unexpected,
            int finalStock,
            long ordersCreated,
            long orderItemsCreated,
            long elapsedNanos,
            long[] requestNanos
    ) {
        int poolSize = dataSource instanceof HikariDataSource hikari
                ? hikari.getMaximumPoolSize()
                : -1;

        long[] sorted = requestNanos.clone();
        Arrays.sort(sorted);
        double p50Ms = sorted[sorted.length / 2] / 1_000_000.0;
        double maxMs = sorted[sorted.length - 1] / 1_000_000.0;

        log.info(String.format(
                "[CONCURRENCY] stock=%d threads=%d pool=%d success=%d rejected=%d "
                        + "unexpected=%d finalStock=%d ordersCreated=%d orderItemsCreated=%d "
                        + "totalMs=%.1f requestP50Ms=%.1f requestMaxMs=%.1f",
                initialStock, threadCount, poolSize, success, rejected,
                unexpected, finalStock, ordersCreated, orderItemsCreated,
                elapsedNanos / 1_000_000.0, p50Ms, maxMs
        ));
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
