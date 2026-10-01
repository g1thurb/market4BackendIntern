package com.seyoon.portfolio.support;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public final class BasketTestFixture {

    public static final UUID TEST_USER_UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    public static final UUID EMPTY_TEST_USER_UUID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    private BasketTestFixture() {
    }

    public static void reset(JdbcTemplate jdbcTemplate) {

        // 테스트에서 사용하는 상품 상태 고정
        jdbcTemplate.update("""
                UPDATE items
                SET available = 10
                WHERE item_code IN (1, 2)
                """);

        // 1111 user basket item 제거
        jdbcTemplate.update("""
                DELETE FROM basket_items
                WHERE basket_id IN (
                    SELECT basket_id
                    FROM user_basket
                    WHERE user_uuid = ?
                )
                """,
                TEST_USER_UUID
        );

        // 3333 user basket item 제거
        jdbcTemplate.update("""
                DELETE FROM basket_items
                WHERE basket_id IN (
                    SELECT basket_id
                    FROM user_basket
                    WHERE user_uuid = ?
                )
                """,
                EMPTY_TEST_USER_UUID
        );

        // basket 제거
        jdbcTemplate.update(
                "DELETE FROM user_basket WHERE user_uuid = ?",
                TEST_USER_UUID
        );

        jdbcTemplate.update(
                "DELETE FROM user_basket WHERE user_uuid = ?",
                EMPTY_TEST_USER_UUID
        );

        // 1111 user만 basket 생성
        jdbcTemplate.update(
                "INSERT INTO user_basket (user_uuid) VALUES (?)",
                TEST_USER_UUID
        );

        Long basketId = jdbcTemplate.queryForObject(
                """
                SELECT basket_id
                FROM user_basket
                WHERE user_uuid = ?
                """,
                Long.class,
                TEST_USER_UUID
        );

        // item 1 × 2
        jdbcTemplate.update("""
                INSERT INTO basket_items (
                    basket_id,
                    item_id,
                    quantity
                )
                VALUES (?, 1, 2)
                """,
                basketId
        );

        // item 2 × 1
        jdbcTemplate.update("""
                INSERT INTO basket_items (
                    basket_id,
                    item_id,
                    quantity
                )
                VALUES (?, 2, 1)
                """,
                basketId
        );
    }
}