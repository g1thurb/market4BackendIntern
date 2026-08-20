package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.BasketItem;
import com.seyoon.portfolio.entity.Item;
import com.seyoon.portfolio.entity.UserBasket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RepositoryQueryTest {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private UserBasketRepository userBasketRepository;

    private static final UUID TEST_USER_UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private BasketItemRepository basketItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findUserById() {
        var user = userInfoRepository.findById(TEST_USER_UUID);

        assertTrue(user.isPresent());
    }

    @Test
    void findBasketByUserUuid() {
        UserBasket basket = userBasketRepository
                .findByUserInfo_Uuid(TEST_USER_UUID)
                .orElseThrow();

        assertNotNull(basket);
        assertNotNull(basket.getBasketId());
    }

    @Test
    void findBasketItemsByBasketId() {
        List<BasketItem> basketItems =
                basketItemRepository.findByUserBasket_BasketId(1L);
        assertFalse(basketItems.isEmpty());
    }

    @Test
    void findBasketItemByBasketAndItem() {
        UserBasket basket = userBasketRepository.findById(1L)
                .orElseThrow(() ->
                        new AssertionError("basket_id=1 does not exist"));

        Item item = itemRepository.findById(1L)
                .orElseThrow(() ->
                        new AssertionError("item_code=1 does not exist"));

        BasketItem basketItem =
                basketItemRepository.findByUserBasketAndItem(basket, item)
                        .orElseThrow(() ->
                                new AssertionError(
                                        "BasketItem does not exist for basket=1, item=1"
                                ));

        assertNotNull(basketItem);
    }
}