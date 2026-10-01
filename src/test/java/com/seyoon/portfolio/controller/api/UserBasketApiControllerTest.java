package com.seyoon.portfolio.controller.api;

import com.seyoon.portfolio.support.BasketTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.seyoon.portfolio.support.BasketTestFixture.EMPTY_TEST_USER_UUID;
import static com.seyoon.portfolio.support.BasketTestFixture.TEST_USER_UUID;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@Rollback
class UserBasketApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        BasketTestFixture.reset(jdbcTemplate);
    }

    @Test
    void getBasket_returns200AndBasket()
            throws Exception {

        mockMvc.perform(
                        get("/api/user/basket")
                                .param(
                                        "userUuid",
                                        TEST_USER_UUID.toString()
                                )
                )
                .andExpect(status().isOk())

                // 실제 PK 값 자체는 테스트 대상이 아님
                .andExpect(
                        jsonPath("$.basketId")
                                .isNumber()
                )

                .andExpect(
                        jsonPath("$.items.length()")
                                .value(2)
                )

                // 배열 순서에도 의존하지 않음
                .andExpect(
                        jsonPath("$.items[*].itemCode")
                                .value(
                                        containsInAnyOrder(
                                                1,
                                                2
                                        )
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.items[?(@.itemCode == 1)].quantity"
                        ).value(
                                hasItem(2)
                        )
                )

                .andExpect(
                        jsonPath(
                                "$.items[?(@.itemCode == 2)].quantity"
                        ).value(
                                hasItem(1)
                        )
                );
    }

    @Test
    void addItem_returns201()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/user/basket/items/{itemCode}",
                                1L
                        )
                                .param(
                                        "userUuid",
                                        EMPTY_TEST_USER_UUID.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                            "quantity": 2
                                        }
                                        """)
                )
                .andExpect(
                        status().isCreated()
                );
    }

    @Test
    void addItem_invalidQuantity_returns400()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/user/basket/items/{itemCode}",
                                1L
                        )
                                .param(
                                        "userUuid",
                                        EMPTY_TEST_USER_UUID.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                            "quantity": 0
                                        }
                                        """)
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.api")
                                .value("INVALID_QUANTITY")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Quantity must be greater than zero"
                                )
                );
    }

    @Test
    void addItem_insufficientStock_returns409()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/user/basket/items/{itemCode}",
                                1L
                        )
                                .param(
                                        "userUuid",
                                        EMPTY_TEST_USER_UUID.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                            "quantity": 20
                                        }
                                        """)
                )
                .andExpect(
                        status().isConflict()
                )
                .andExpect(
                        jsonPath("$.api")
                                .value("INSUFFICIENT_STOCK")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Quantity exceeded")
                );
    }

    @Test
    void addItem_unknownItem_returns404()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/user/basket/items/{itemCode}",
                                999999L
                        )
                                .param(
                                        "userUuid",
                                        EMPTY_TEST_USER_UUID.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                            "quantity": 1
                                        }
                                        """)
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        jsonPath("$.api")
                                .value("NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Item not found")
                );
    }

    @Test
    void changeQuantity_returns204()
            throws Exception {

        mockMvc.perform(
                        patch(
                                "/api/user/basket/items/{itemCode}",
                                1L
                        )
                                .param(
                                        "userUuid",
                                        TEST_USER_UUID.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                            "quantity": 5
                                        }
                                        """)
                )
                .andExpect(
                        status().isNoContent()
                );
    }

    @Test
    void deleteItem_returns204()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/user/basket/items/{itemCode}",
                                1L
                        )
                                .param(
                                        "userUuid",
                                        TEST_USER_UUID.toString()
                                )
                )
                .andExpect(
                        status().isNoContent()
                );
    }
}