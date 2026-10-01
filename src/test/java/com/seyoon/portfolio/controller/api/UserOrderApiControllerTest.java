package com.seyoon.portfolio.controller.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//TODO Check HTTP request & response goes properly
// param에 Long인 data는 L 안 붙인다
// 반대로 String과 enum은 반드시 따옴표"" 붙이기

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class UserOrderApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void getUserBasketOrdersPreview_returns200AndOrdersPreviewResponse() throws Exception {
        mockMvc.perform(
                        post("/api/user/orders/preview/basket")
                                .param(
                                        "userUuid",
                                        "11111111-1111-1111-1111-111111111111"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "couponCodes": ["TEST_PRICE_1000"]
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutTotalAmount").value(34000));
    }

    @Test
    void getUserItemOrdersPreview_returns200AndCreateOrdersResponse() throws Exception {
        mockMvc.perform(
                        post("/api/user/orders/preview/item")
                                .param(
                                        "userUuid",
                                        "11111111-1111-1111-1111-111111111111"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "itemCode": 1,
                                            "quantity": 2,
                                            "couponCode": "TEST_PRICE_1000"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutTotalAmount").value(19000));
    }

    @Test
    void getUserBasketOrders_returns201AndCreateOrdersResponse() throws Exception {
        mockMvc.perform(
                        post("/api/user/orders/basket")
                                .param(
                                        "userUuid",
                                        "11111111-1111-1111-1111-111111111111"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "couponCodes": ["TEST_PRICE_1000"],
                                            "addressId": 3,
                                            "purchaseType": "CARD",
                                            "paymentId": 1
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checkoutId").isNumber())
                .andExpect(jsonPath("$.orderIds").isArray())
                .andExpect(jsonPath("$.orderIds.length()").value(1));
    }

    @Test
    void getUserItemOrders_returns201AndCreateOrdersResponse() throws Exception {
        mockMvc.perform(
                        post("/api/user/orders/item")
                                .param(
                                        "userUuid",
                                        "11111111-1111-1111-1111-111111111111"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "itemCode": 1,
                                            "quantity": 2,
                                            "couponCode": "TEST_PRICE_1000",
                                            "addressId": 3,
                                             "purchaseType": "CARD",
                                             "paymentId": 1
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checkoutId").isNumber())
                .andExpect(jsonPath("$.orderIds").isArray())
                .andExpect(jsonPath("$.orderIds.length()").value(1));
    }
}
