package com.seyoon.portfolio.controller.api;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class UserBasketApiControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    void getBasket_returns200AndBasket() throws Exception {

        mockMvc.perform(
                        get("/api/user/basket")
                                .param(
                                        "userUuid",
                                        "11111111-1111-1111-1111-111111111111"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basketId").value(1))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].itemCode").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }


    @Test
    void addItem_returns201() throws Exception {

        mockMvc.perform(
                        post("/api/user/basket/items/{itemCode}", 1L)
                                .param(
                                        "userUuid",
                                        "33333333-3333-3333-3333-333333333333"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "quantity": 2
                                        }
                                        """)
                )
                .andExpect(status().isCreated());
    }


    @Test
    void addItem_invalidQuantity_returns400() throws Exception {

        mockMvc.perform(
                        post("/api/user/basket/items/{itemCode}", 1L)
                                .param(
                                        "userUuid",
                                        "33333333-3333-3333-3333-333333333333"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "quantity": 0
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.api").value("INVALID_QUANTITY"))
                .andExpect(jsonPath("$.message")
                        .value("Quantity must be greater than zero"));
    }


    @Test
    void addItem_insufficientStock_returns409() throws Exception {

        mockMvc.perform(
                        post("/api/user/basket/items/{itemCode}", 1L)
                                .param(
                                        "userUuid",
                                        "33333333-3333-3333-3333-333333333333"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "quantity": 20
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.api").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.message").value("Quantity exceeded"));
    }


    @Test
    void addItem_unknownItem_returns404() throws Exception {

        mockMvc.perform(
                        post("/api/user/basket/items/{itemCode}", 999999L)
                                .param(
                                        "userUuid",
                                        "33333333-3333-3333-3333-333333333333"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "quantity": 1
                                        }
                                        """)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.api").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Item not found"));
    }


    @Test
    void changeQuantity_returns204() throws Exception {

        mockMvc.perform(
                        patch("/api/user/basket/items/{itemCode}", 1L)
                                .param(
                                        "userUuid",
                                        "11111111-1111-1111-1111-111111111111"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "quantity": 5
                                        }
                                        """)
                )
                .andExpect(status().isNoContent());
    }


    @Test
    void deleteItem_returns204() throws Exception {

        mockMvc.perform(
                        delete("/api/user/basket/items/{itemCode}", 1L)
                                .param(
                                        "userUuid",
                                        "11111111-1111-1111-1111-111111111111"
                                )
                )
                .andExpect(status().isNoContent());
    }
}