package com.seyoon.portfolio.service;

import com.seyoon.portfolio.dto.PurchaseType;
import com.seyoon.portfolio.dto.response.CreateOrdersResponse;
import com.seyoon.portfolio.dto.response.OrdersPreviewResponse;
import com.seyoon.portfolio.dto.response.OrdersPreviewResponseStoreGroup;
import com.seyoon.portfolio.entity.Checkout;
import com.seyoon.portfolio.entity.OrderCoupon;
import com.seyoon.portfolio.entity.OrderEntity;
import com.seyoon.portfolio.entity.type.CheckoutStatus;
import com.seyoon.portfolio.entity.type.OrderStatus;
import com.seyoon.portfolio.exception.CouponNotFoundException;
import com.seyoon.portfolio.exception.CouponNotMatchException;
import com.seyoon.portfolio.exception.InsufficientStockException;
import com.seyoon.portfolio.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


//TODO
// DONE 1. newOrderItem_success
//    - order 생성
//    - orderItem 생성
//    - stock 감소
// DONE 2. newOrderItem_withCoupon_success
// DONE 3. newOrderBasket_success
// 4. newOrderBasket_multiStore_success -> store 1개라서 보류
// 5. snapshot_success
//    - item name
//    - item price
//    - delivery address
// 6. coupon validation
//    - invalid coupon
//    - expired coupon
// 7. insufficientStock_fails
// 별도 transaction test
// 8. basket 일부 상품 stock 부족 -> 전체 rollback
// 별도 concurrency test
// 9. 마지막 재고 동시 주문 -> 정확히 1건만 성공
//다만 8, 9는 별도 취급해야 해.
// 특히 rollback 테스트를 class-level @Transactional 안에서 Service 예외 직후 확인하면,
// Service가 테스트의 바깥 transaction에 합류하기 때문에 “실제로 transaction 종료 후 rollback됐는지”를 정확히 검증하기 어렵다.
// concurrency는 더더욱 class-level transaction을 쓰면 안 되고.

@SpringBootTest
@Transactional
class UserOrderServiceTest {

    @Autowired
    private UserOrderService userOrderService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private OrderCouponRepository orderCouponRepository;

    @Autowired
    private BasketItemRepository basketItemRepository;

    @Autowired
    private UserBasketRepository userBasketRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final UUID BASKET_TEST_USER_UUID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID DIRECT_TEST_USER_UUID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID DIRECT_TEST_SELLER_UUID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Long DIRECT_TEST_ITEM_ID = 1L;
    private static final Long DIRECT_TEST_ADDRESS_ID = 3L;
    private static final Long DIRECT_TEST_PAYMENT_ID = 1L;

    @Test
    void newOrderItem_success() {
        // given
        Long itemId = DIRECT_TEST_ITEM_ID;
        var itemBefore = itemRepository.findById(itemId).orElseThrow();
        int stockBefore = itemBefore.getAvailable();
        BigDecimal priceBefore = itemBefore.getPrice();
        String itemNameBefore = itemBefore.getItemName();
        int quantity = 1;
        // when
        CreateOrdersResponse response =
                userOrderService.newOrderItem(
                        DIRECT_TEST_USER_UUID,
                        itemId,
                        quantity,
                        null, //"TEST_PRICE_1000"도 나중에
                        DIRECT_TEST_ADDRESS_ID,
                        PurchaseType.CARD,
                        DIRECT_TEST_PAYMENT_ID
                );
        entityManager.flush();
        entityManager.clear();
        // then
        assertNotNull(response);
        assertNotNull(response.checkoutId());
        assertEquals(1, response.orderIds().size());

        var itemAfter = itemRepository.findById(itemId).orElseThrow();

        assertEquals(
                stockBefore - quantity,
                itemAfter.getAvailable()
        );
        Long checkoutId = response.checkoutId();
        Long orderId = response.orderIds().getFirst();

        var orderItems =
                orderItemRepository.findAllByOrderEntity_OrderId(orderId);

        assertEquals(1, orderItems.size());

        var orderItem = orderItems.getFirst();

        assertEquals(orderId, orderItem.getOrderEntity().getOrderId());
        assertEquals(itemId, orderItem.getItem().getItemCode());
        assertEquals(quantity, orderItem.getQuantity());
        assertEquals(priceBefore, orderItem.getUnitPriceAtPurchase());
        assertEquals(itemNameBefore, orderItem.getItemNameSnapshot());

        var checkout =  checkoutRepository.findById(checkoutId).orElseThrow();
        var order = orderRepository.findById(orderId).orElseThrow();

        assertEquals(checkoutId, order.getCheckout().getCheckoutId());
        assertEquals(DIRECT_TEST_USER_UUID, order.getUserInfo().getUuid());
        assertEquals(DIRECT_TEST_SELLER_UUID, order.getSellerInfo().getStoreUuid());
        assertEquals(OrderStatus.PAID, order.getOrderStatus());
        assertFalse(order.isConfirmExtended());
        assertEquals(
                "서울특별시 강남구 테헤란로 123",
                order.getShippingAddressSnapshot()
        );

        assertEquals(DIRECT_TEST_USER_UUID, checkout.getUserInfo().getUuid());
        BigDecimal expectedTotal =
                priceBefore.multiply(BigDecimal.valueOf(quantity));

        assertEquals(
                0,
                expectedTotal.compareTo(checkout.getTotalAmount())
        );
    }

    @Test
    void newOrderItem_withCoupon_success() {
        // given
        Long itemId = DIRECT_TEST_ITEM_ID;
        int quantity = 1;
        String couponCode = "TEST_PRICE_1000";

        var itemBefore =
                itemRepository.findById(itemId).orElseThrow();

        int stockBefore = itemBefore.getAvailable();
        BigDecimal priceBefore = itemBefore.getPrice();

        // when
        CreateOrdersResponse response =
                userOrderService.newOrderItem(
                        DIRECT_TEST_USER_UUID,
                        itemId,
                        quantity,
                        couponCode,
                        DIRECT_TEST_ADDRESS_ID,
                        PurchaseType.CARD,
                        DIRECT_TEST_PAYMENT_ID
                );

        entityManager.flush();
        entityManager.clear();

        // then
        Long checkoutId = response.checkoutId();
        Long orderId = response.orderIds().getFirst();

        var itemAfter =
                itemRepository.findById(itemId).orElseThrow();

        assertEquals(
                stockBefore - quantity,
                itemAfter.getAvailable()
        );

        var checkout =
                checkoutRepository.findById(checkoutId).orElseThrow();

        BigDecimal subtotal =
                priceBefore.multiply(BigDecimal.valueOf(quantity));

        BigDecimal expectedTotal =
                subtotal.subtract(BigDecimal.valueOf(1000));

        assertEquals(
                0,
                expectedTotal.compareTo(checkout.getTotalAmount())
        );

        var orderCoupon =
                orderCouponRepository
                        .findByOrderItem_OrderEntity_OrderId(orderId)
                        .orElseThrow();

        assertEquals(
                couponCode,
                orderCoupon.getCoupon().getCouponCode()
        );
    }

    @Test
    void newOrderBasket_success() {
        // given
        List<String> couponCodes = List.of("TEST_ITEM2_ONLY");
        PurchaseType purchaseType = PurchaseType.CARD;

        Long basketId = userBasketRepository
                .findByUserInfo_Uuid(DIRECT_TEST_USER_UUID)
                .orElseThrow()
                .getBasketId();

        var item1Before = itemRepository.findById(1L).orElseThrow();
        var item2Before = itemRepository.findById(2L).orElseThrow();

        int item1StockBefore = item1Before.getAvailable();
        int item2StockBefore = item2Before.getAvailable();

        OrdersPreviewResponse preview =
                userOrderService.newPreviewBasket(
                        DIRECT_TEST_USER_UUID,
                        couponCodes
                );

        // when
        CreateOrdersResponse response =
                userOrderService.newOrderBasket(
                        DIRECT_TEST_USER_UUID,
                        DIRECT_TEST_ADDRESS_ID,
                        couponCodes,
                        purchaseType,
                        DIRECT_TEST_PAYMENT_ID
                );

        entityManager.flush();
        entityManager.clear();

        // then - response
        assertNotNull(response);
        assertNotNull(response.checkoutId());
        assertEquals(1, response.orderIds().size());

        Long checkoutId = response.checkoutId();
        Long orderId = response.orderIds().getFirst();

        // then - checkout
        var checkout = checkoutRepository
                .findById(checkoutId)
                .orElseThrow();

        assertEquals(DIRECT_TEST_USER_UUID, checkout.getUserInfo().getUuid());
        assertEquals(CheckoutStatus.CREATED, checkout.getCheckoutStatus());

        assertEquals(
                0,
                preview.checkoutTotalAmount()
                        .compareTo(checkout.getTotalAmount())
        );

        // then - stock
        var item1After = itemRepository.findById(1L).orElseThrow();
        var item2After = itemRepository.findById(2L).orElseThrow();

        assertEquals(
                item1StockBefore - 2,
                item1After.getAvailable()
        );

        assertEquals(
                item2StockBefore - 1,
                item2After.getAvailable()
        );

        // then - order
        var order = orderRepository.findById(orderId).orElseThrow();

        assertEquals(checkoutId, order.getCheckout().getCheckoutId());
        assertEquals(DIRECT_TEST_USER_UUID, order.getUserInfo().getUuid());
        assertEquals(DIRECT_TEST_SELLER_UUID, order.getSellerInfo().getStoreUuid());
        assertEquals(OrderStatus.PAID, order.getOrderStatus());
        assertFalse(order.isConfirmExtended());

        assertEquals(
                "서울특별시 강남구 테헤란로 123",
                order.getShippingAddressSnapshot()
        );

        assertEquals(
                "서울특별시 강남구 테헤란로 123",
                order.getDeliveryAddress()
        );

        // then - order items
        var orderItems =
                orderItemRepository.findAllByOrderEntity_OrderId(orderId);

        assertEquals(2, orderItems.size());

        var item1OrderItem = orderItems.stream()
                .filter(orderItem ->
                        orderItem.getItem().getItemCode().equals(1L))
                .findFirst()
                .orElseThrow();

        var item2OrderItem = orderItems.stream()
                .filter(orderItem ->
                        orderItem.getItem().getItemCode().equals(2L))
                .findFirst()
                .orElseThrow();

        assertEquals(item1Before.getPrice(), item1OrderItem.getUnitPriceAtPurchase());
        assertEquals(item1Before.getItemName(), item1OrderItem.getItemNameSnapshot());

        assertEquals(item2Before.getPrice(), item2OrderItem.getUnitPriceAtPurchase());
        assertEquals(item2Before.getItemName(), item2OrderItem.getItemNameSnapshot());

        assertEquals(2, item1OrderItem.getQuantity());
        assertEquals(1, item2OrderItem.getQuantity());

        // then - basket cleared
        assertTrue(
                basketItemRepository
                        .findAllByUserBasket_BasketId(basketId)
                        .isEmpty()
        );
    }

    // AI Built Test Code
    @Test
    void snapshot_success() {
        // given
        Long itemId = DIRECT_TEST_ITEM_ID;

        var itemBefore = itemRepository.findById(itemId).orElseThrow();

        String originalItemName = itemBefore.getItemName();
        BigDecimal originalPrice = itemBefore.getPrice();

        String originalAddress = (String) entityManager
                .createNativeQuery("""
                    SELECT address
                    FROM user_address_saved
                    WHERE address_id = :addressId
                    """)
                .setParameter("addressId", DIRECT_TEST_ADDRESS_ID)
                .getSingleResult();

        CreateOrdersResponse response =
                userOrderService.newOrderItem(
                        DIRECT_TEST_USER_UUID,
                        itemId,
                        1,
                        null,
                        DIRECT_TEST_ADDRESS_ID,
                        PurchaseType.CARD,
                        DIRECT_TEST_PAYMENT_ID
                );

        entityManager.flush();

        Long orderId = response.orderIds().getFirst();

        // 주문이 끝난 뒤 원본 데이터 변경
        entityManager.createNativeQuery("""
            UPDATE items
            SET item_name = :newName,
                price = :newPrice
            WHERE item_code = :itemId
            """)
                .setParameter("newName", "changed_item_name")
                .setParameter("newPrice", new BigDecimal("99999.00"))
                .setParameter("itemId", itemId)
                .executeUpdate();

        entityManager.createNativeQuery("""
            UPDATE user_address_saved
            SET address = :newAddress
            WHERE address_id = :addressId
            """)
                .setParameter("newAddress", "변경된 테스트 주소")
                .setParameter("addressId", DIRECT_TEST_ADDRESS_ID)
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        // when
        var order = orderRepository.findById(orderId).orElseThrow();

        var orderItem = orderItemRepository
                .findAllByOrderEntity_OrderId(orderId)
                .getFirst();

        // then
        assertEquals(
                originalItemName,
                orderItem.getItemNameSnapshot()
        );

        assertEquals(
                0,
                originalPrice.compareTo(
                        orderItem.getUnitPriceAtPurchase()
                )
        );

        assertEquals(
                originalAddress,
                order.getShippingAddressSnapshot()
        );
    }

    @Test
    void insufficientStock_fails() {
        // given
        var item = itemRepository
                .findById(DIRECT_TEST_ITEM_ID)
                .orElseThrow();

        int quantityOverStock =
                item.getAvailable() + 1;

        // when & then
        assertThrows(
                InsufficientStockException.class,
                () -> userOrderService.newOrderItem(
                        DIRECT_TEST_USER_UUID,
                        DIRECT_TEST_ITEM_ID,
                        quantityOverStock,
                        null,
                        DIRECT_TEST_ADDRESS_ID,
                        PurchaseType.CARD,
                        DIRECT_TEST_PAYMENT_ID
                )
        );
    }

    @Test
    void invalidCoupon_fails() {
        assertThrows(
                /* 네 실제 Coupon Not Found 예외 클래스 */
                CouponNotFoundException.class,
                () -> userOrderService.newOrderItem(
                        DIRECT_TEST_USER_UUID,
                        DIRECT_TEST_ITEM_ID,
                        1,
                        "THIS_COUPON_DOES_NOT_EXIST",
                        DIRECT_TEST_ADDRESS_ID,
                        PurchaseType.CARD,
                        DIRECT_TEST_PAYMENT_ID
                )
        );
    }

    @Test
    void expiredCoupon_fails() {
        assertThrows(
                /* 네 실제 Expired Coupon 예외 클래스 */
                CouponNotMatchException.class,
                () -> userOrderService.newOrderItem(
                        DIRECT_TEST_USER_UUID,
                        DIRECT_TEST_ITEM_ID,
                        1,
                        "TEST_EXPIRED",
                        DIRECT_TEST_ADDRESS_ID,
                        PurchaseType.CARD,
                        DIRECT_TEST_PAYMENT_ID
                )
        );
    }

//    @Test
//    void newOrderBasket_multiStore_success() {}
}