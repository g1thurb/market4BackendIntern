package com.seyoon.portfolio.repository;

import com.seyoon.portfolio.entity.BasketItem;
import com.seyoon.portfolio.entity.Item;
import com.seyoon.portfolio.entity.UserBasket;
import com.seyoon.portfolio.support.BasketTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.seyoon.portfolio.support.BasketTestFixture.TEST_USER_UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
class RepositoryQueryTest {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private UserBasketRepository userBasketRepository;

    @Autowired
    private BasketItemRepository basketItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        BasketTestFixture.reset(jdbcTemplate);
    }

    @Test
    void findUserById() {
        var user =
                userInfoRepository.findById(TEST_USER_UUID);

        assertTrue(user.isPresent());
    }

    @Test
    void findBasketByUserUuid() {
        UserBasket basket =
                userBasketRepository
                        .findByUserInfo_Uuid(TEST_USER_UUID)
                        .orElseThrow();

        assertNotNull(basket);
        assertNotNull(basket.getBasketId());
    }

    @Test
    void findBasketItemsByBasketId() {

        Long basketId =
                userBasketRepository
                        .findByUserInfo_Uuid(TEST_USER_UUID)
                        .orElseThrow()
                        .getBasketId();

        List<BasketItem> basketItems =
                basketItemRepository
                        .findAllByUserBasket_BasketId(basketId);

        assertEquals(2, basketItems.size());
    }

    @Test
    void findBasketItemByBasketAndItem() {

        UserBasket basket =
                userBasketRepository
                        .findByUserInfo_Uuid(TEST_USER_UUID)
                        .orElseThrow();

        Item item =
                itemRepository.findById(1L)
                        .orElseThrow();

        BasketItem basketItem =
                basketItemRepository
                        .findByUserBasketAndItem(basket, item)
                        .orElseThrow();

        assertNotNull(basketItem);
        assertEquals(2, basketItem.getQuantity());
    }
}