package com.seyoon.portfolio.support;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Coupon fixture rows live in the development database with an absolute due_date,
 * so tests that rely on "valid" coupons start failing once that date passes.
 *
 * Call this from @BeforeEach inside a test transaction: the due dates are moved
 * relative to now() and the change is rolled back with the test.
 */
public final class CouponFixtures {

    public static final String EXPIRED_COUPON_CODE = "TEST_EXPIRED";

    private CouponFixtures() {
    }

    public static void refreshDueDates(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(
                "UPDATE coupons SET due_date = now() + interval '30 days' "
                        + "WHERE coupon_code LIKE 'TEST\\_%' AND coupon_code <> ?",
                EXPIRED_COUPON_CODE
        );
        jdbcTemplate.update(
                "UPDATE coupons SET due_date = now() - interval '1 day' "
                        + "WHERE coupon_code = ?",
                EXPIRED_COUPON_CODE
        );
    }
}
