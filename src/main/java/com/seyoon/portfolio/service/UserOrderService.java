package com.seyoon.portfolio.service;

import com.seyoon.portfolio.dto.PurchaseType;
import com.seyoon.portfolio.dto.response.*;
import com.seyoon.portfolio.entity.*;
import com.seyoon.portfolio.entity.type.CheckoutStatus;
import com.seyoon.portfolio.entity.type.DiscountTypeSnapshot;
import com.seyoon.portfolio.entity.type.OrderStatus;
import com.seyoon.portfolio.exception.*;
import com.seyoon.portfolio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

// TODO[ORDER-COUPON-SCHEMA]:
// OrderCoupon relation was changed from OrderEntity to OrderItem.
// Synchronize PostgreSQL DDL with JPA mapping:
// - PK/FK: order_item_id
// - FK -> order_items(order_item_id)
// - update existing schema / test fixtures if necessary
// Current ddl-auto=validate causes ApplicationContext/tests to fail
// until DB schema matches the Entity.
// TODO[ORDER-TEST]:
// Order-related schema change currently prevents Spring context loading.
// Re-run full tests after ORDER-COUPON-SCHEMA migration is completed.

@Service
public class UserOrderService {

    private final UserPaymentService userPaymentService; //static 처리해서 함수들을 instacnceless로 만드려다 변경
    private final UserBasketRepository userBasketRepository;
    private final BasketItemRepository basketItemRepository;
    private final CouponRepository couponRepository;
    private final ItemRepository itemRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserAddressSavedRepository userAddressSavedRepository;
    private final UserPaymentSavedRepository userPaymentSavedRepository;
    private final CheckoutRepository checkoutRepository;
    private final OrderRepository orderRepository;
    private final OrderCouponRepository orderCouponRepository;
    private final OrderItemRepository orderItemRepository;

    public UserOrderService(UserPaymentService userPaymentService,
                            UserBasketRepository userBasketRepository,
                            BasketItemRepository basketItemRepository,
                            CouponRepository couponRepository,
                            ItemRepository itemRepository,
                            UserInfoRepository userInfoRepository,
                            UserAddressSavedRepository userAddressSavedRepository,
                            UserPaymentSavedRepository userPaymentSavedRepository,
                            CheckoutRepository checkoutRepository,
                            OrderRepository orderRepository,
                            OrderCouponRepository orderCouponRepository,
                            OrderItemRepository orderItemRepository) {
        this.userPaymentService = userPaymentService;
        this.userBasketRepository = userBasketRepository;
        this.basketItemRepository = basketItemRepository;
        this.couponRepository = couponRepository;
        this.itemRepository = itemRepository;
        this.userInfoRepository = userInfoRepository;
        this.userAddressSavedRepository = userAddressSavedRepository;
        this.userPaymentSavedRepository = userPaymentSavedRepository;
        this.checkoutRepository = checkoutRepository;
        this.orderRepository = orderRepository;
        this.orderCouponRepository = orderCouponRepository;
        this.orderItemRepository = orderItemRepository;
    }

    //Preview에서는 Order를 만들면 안 됨 -> @Transactional(readOnly = true) 붙이기

// TODO[ORDER-COUPON]:
// Handle coupon expiration.
// Prevent discountAmount from exceeding orderSubtotal.
// Handle nullable discountLimit if DB policy allows null.
// orderpreview관련 코드들은 주문 전 페이지. 주문시 DB의 order table 생성
// TODO[ORDER-PREVIEW-REFACTOR]:
// Rewrite basket preview using nested store/item structure.
// Align preview calculation with newOrderBasket:
// - support multiple store coupons
// - calculate coupon per item/store with the same V1 policy
// - keep preview and actual order totals consistent
// - extract shared price/coupon calculation if useful
// TODO[ORDER-PREVIEW-COUPON]:
// Current preview accepts only one couponCode.
// Change to multiple coupon codes when basket preview is rewritten.
    @Transactional(readOnly = true)
    public OrdersPreviewResponse newPreviewBasket(
            UUID userUuid,
            String couponCode
    ) {
        UserBasket userBasket = userBasketRepository.findByUserInfo_Uuid(userUuid)
                .orElse(null);

        if (userBasket == null) {
            return new OrdersPreviewResponse(
                    BigDecimal.ZERO,
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        List<BasketItem> basketItems = basketItemRepository.findByUserBasket_BasketId(userBasket.getBasketId());

        // storeUuid ASC
        basketItems.sort(Comparator.comparing(BasketItem::getItemStoreUUID));

        List<OrdersPreviewResponseStoreGroup> storeGroups = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        BigDecimal checkoutTotalAmount = BigDecimal.ZERO;

        UUID currentStoreUuid = null;
        String currentStoreName = null;

        List<OrdersPreviewResponseItemGroup> currentItems = new ArrayList<>();
        BigDecimal currentSubtotal = BigDecimal.ZERO;

        Coupon coupon = null;

        if (couponCode != null) {
            coupon = couponRepository.findById(couponCode)
                    .orElseThrow(() ->
                            new CouponNotFoundException("Coupon not found"));
        }

        for (BasketItem basketItem : basketItems) {
            Item item = basketItem.getItem();
            SellerInfo sellerInfo = item.getSellerInfo();

            UUID storeUuid = sellerInfo.getStoreUuid();

            //이전 store와 다른 store가 등장했다면 지금까지 계산하던 store를 먼저 완성한다.
            if (currentStoreUuid != null &&
                    !currentStoreUuid.equals(storeUuid)) {

                BigDecimal deliveryFee = BigDecimal.ZERO;
                BigDecimal discountAmount = BigDecimal.ZERO;

// TODO[ORDER-COUPON-01]:
// Apply coupon only to matching store/item.
// For multi-store basket, do not fail on non-target stores.

// TODO[ORDER-COUPON-02]:
// Handle coupon expiration.

// TODO[ORDER-COUPON-03]:
// Apply discountLimit and prevent finalAmount < 0.

// TODO[ORDER-COUPON-04]:
// Extract duplicated coupon calculation into private method.
                //coupon 계산
                if (coupon != null) {
                    // 쿠폰 적용 검사
                    if(coupon.getSellerInfo().getStoreUuid().equals(currentStoreUuid)) {
                        if (coupon.getItem()==null || coupon.getItem().equals(item)) {
                            if(OffsetDateTime.now().isBefore(coupon.getDueDate())){
                                switch (coupon.getDiscountType()) {
                                    case PRICE -> discountAmount =  coupon.getDiscountAmount();
                                    case PERCENT -> discountAmount = currentSubtotal
                                            .multiply(coupon.getDiscountAmount())
                                            .divide(BigDecimal.valueOf(100));
                                }
                                if(discountAmount.compareTo(coupon.getDiscountLimit())>0) {
                                    discountAmount = coupon.getDiscountLimit();
                                }
                            }
                            // else  { // due-date exceed; make exception;} do nothing; after time, make exception and action
                        }
                        else {throw new CouponNotMatchException("Coupon not match");}
                    }
                    else {throw new CouponNotMatchException("Coupon not match");}
                }

                BigDecimal finalAmount = currentSubtotal.add(deliveryFee).subtract(discountAmount);

                storeGroups.add(
                        new OrdersPreviewResponseStoreGroup(
                                currentStoreUuid,
                                currentStoreName,
                                currentSubtotal,
                                deliveryFee,
                                discountAmount,
                                finalAmount,
                                currentItems
                        )
                );

                checkoutTotalAmount = checkoutTotalAmount.add(finalAmount);

                // 다음 store 계산을 위해 초기화
                currentItems = new ArrayList<>();
                currentSubtotal = BigDecimal.ZERO;
            }

            // 첫 store이거나 새로운 store라면 현재 store 정보 갱신
            if (!storeUuid.equals(currentStoreUuid)) {
                currentStoreUuid = storeUuid;
                currentStoreName = sellerInfo.getStoreName();
            }

            String mainImage = item.getMainImages().isEmpty()
                            ? null
                            : item.getMainImages().getFirst();

            BigDecimal unitPrice = item.getPrice();
            int quantity = basketItem.getQuantity();

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            // 재고 문제는 preview에서는 warning으로 표시할지,
            // preview 자체를 실패시킬지 정책 결정 필요
            if (item.getAvailable() == 0) {
                warnings.add(
                        item.getItemName() + " is out of stock"
                );
            } else if (item.getAvailable() < quantity) {
                warnings.add(
                        item.getItemName() + " has insufficient stock"
                );
            }

            currentItems.add(
                    new OrdersPreviewResponseItemGroup(
                            item.getItemCode(),
                            item.getItemName(),
                            quantity,
                            unitPrice,
                            lineTotal,
                            mainImage
                    )
            );

            // 어떤 store든 첫 item 포함해서 무조건 subtotal 증가
            currentSubtotal = currentSubtotal.add(lineTotal);
        }

        /*
         * loop에서는 "다음 store가 등장할 때" 이전 store를 마감했으므로,
         * 마지막 store는 여기서 따로 마감해야 한다.
         */
        if (currentStoreUuid != null) {
            BigDecimal deliveryFee = BigDecimal.ZERO;
            BigDecimal discountAmount = BigDecimal.ZERO;

            //coupon 계산 (위 logic과 동일)
            if(couponCode != null) {
                Item item = basketItems.getLast().getItem();
                SellerInfo sellerInfo = item.getSellerInfo();

// TODO[ORDER-COUPON-01]:
// Apply coupon only to matching store/item.
// For multi-store basket, do not fail on non-target stores.

// TODO[ORDER-COUPON-02]:
// Handle coupon expiration.

// TODO[ORDER-COUPON-03]:
// Apply discountLimit and prevent finalAmount < 0.

// TODO[ORDER-COUPON-04]:
// Extract duplicated coupon calculation into private method.
                UUID storeUuid = sellerInfo.getStoreUuid();
//                Coupon coupon = couponRepository.findById(couponCode)
//                        .orElseThrow(() -> new CouponNotFoundException("Coupon not found"));
                if (coupon != null) {
                    // 쿠폰 적용 검사
                    if (coupon.getSellerInfo().getStoreUuid().equals(currentStoreUuid)) {
                        if (coupon.getItem() == null || coupon.getItem().equals(item)) {
                            if (OffsetDateTime.now().isBefore(coupon.getDueDate())) {
                                switch (coupon.getDiscountType()) {
                                    case PRICE -> discountAmount = coupon.getDiscountAmount();
                                    case PERCENT -> discountAmount = currentSubtotal
                                            .multiply(coupon.getDiscountAmount())
                                            .divide(BigDecimal.valueOf(100));
                                }
                                if (discountAmount.compareTo(coupon.getDiscountLimit()) > 0) {
                                    discountAmount = coupon.getDiscountLimit();
                                }
                            }
                            // else  { // due-date exceed; make exception;} do nothing; after time, make exception and action
                        } else {
                            throw new CouponNotMatchException("Coupon not match");
                        }
                    } else {
                        throw new CouponNotMatchException("Coupon not match");
                    }
                }
            }
            // else{// couponCode == null;} nothing to do

            BigDecimal finalAmount =
                    currentSubtotal
                            .add(deliveryFee)
                            .subtract(discountAmount);

            storeGroups.add(
                    new OrdersPreviewResponseStoreGroup(
                            currentStoreUuid,
                            currentStoreName,
                            currentSubtotal,
                            deliveryFee,
                            discountAmount,
                            finalAmount,
                            currentItems
                    )
            );

            checkoutTotalAmount =
                    checkoutTotalAmount.add(finalAmount);
        }


        // 배송지
        List<OrdersPreviewResponseAvailableAddress> availableAddresses =
                new ArrayList<>();

        List<UserAddressSaved> savedAddresses =
                userAddressSavedRepository.findAllByUserInfo_Uuid(userUuid);

        for (UserAddressSaved savedAddress : savedAddresses) {
            availableAddresses.add(
                    new OrdersPreviewResponseAvailableAddress(
                            savedAddress.getAddressId(),
                            savedAddress.getAddress()
                    )
            );
        }


        // 결제수단
        List<OrdersPreviewResponseAvailablePayment> availablePayments =
                new ArrayList<>();

        List<UserPaymentSaved> savedPayments =
                userPaymentSavedRepository.findAllByUserInfo_Uuid(userUuid);

        for (UserPaymentSaved savedPayment : savedPayments) {
            availablePayments.add(
                    new OrdersPreviewResponseAvailablePayment(
                            savedPayment.getPaymentId(),
                            savedPayment.getPaymentEncryptedData()
                    )
            );
        }

        return new OrdersPreviewResponse(
                checkoutTotalAmount,
                storeGroups,
                availableAddresses,
                availablePayments,
                warnings
        );
    }

    @Transactional(readOnly = true)
    public OrdersPreviewResponse newPreviewItem(
            UUID userUuid,
            Long itemCode,
            int quantity,
            String couponCode
    ) {
        if(quantity <= 0){throw new InvalidQuantityException("Quantity must be greater than 0");}
        Item item = itemRepository.findById(itemCode).orElseThrow(() ->
                new ItemNotFoundException("Item " + itemCode + " not found"));
        if (item.getAvailable() == 0) {throw new ItemOutOfStockException("Item " + itemCode + " is out of stock");}
        if (item.getAvailable() < quantity) {throw new InsufficientStockException("Item " + itemCode + " has insufficient stock");}
        Coupon coupon = null;
        if (couponCode != null) {coupon = couponRepository.findById(couponCode).orElseThrow(() ->
                new CouponNotFoundException("Coupon " + couponCode + " not found"));}

        BigDecimal unitPrice = item.getPrice();
        BigDecimal orderSubtotal =  unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal deliveryFee =  BigDecimal.ZERO;

        BigDecimal discountAmount = null;
        BigDecimal finalAmount = null;

        if (coupon != null) {
            if (!coupon.getSellerInfo().getStoreUuid()
                    .equals(item.getSellerInfo().getStoreUuid())) {
                throw new CouponNotMatchException("Coupon not match");
            }
            if (coupon.getItem() != null &&
                    !coupon.getItem().getItemCode().equals(item.getItemCode())) {
                throw new CouponNotMatchException("Coupon not match");
            }
            switch (coupon.getDiscountType()) {
                case PRICE -> discountAmount = coupon.getDiscountAmount();
                case PERCENT -> discountAmount = orderSubtotal
                        .multiply(coupon.getDiscountAmount()).divide(BigDecimal.valueOf(100));
            }
            if(discountAmount.compareTo(coupon.getDiscountLimit())>0){
                discountAmount = coupon.getDiscountLimit();
            }
            finalAmount = orderSubtotal.subtract(discountAmount);
        }
        else {
            discountAmount = BigDecimal.ZERO;
            finalAmount = orderSubtotal;
        }

        SellerInfo sellerInfo = item.getSellerInfo();
        String mainImage = item.getMainImages().isEmpty() ? null : item.getMainImages().getFirst();

        List<OrdersPreviewResponseItemGroup> ordersPreviewResponseItemGroups = new ArrayList<>();
        ordersPreviewResponseItemGroups.add(new OrdersPreviewResponseItemGroup(
                itemCode,
                item.getItemName(),
                quantity,
                unitPrice,
                orderSubtotal,
                mainImage
        ));

        List<OrdersPreviewResponseStoreGroup> storeGroups = new ArrayList<>();
        storeGroups.add(new OrdersPreviewResponseStoreGroup(
                sellerInfo.getStoreUuid(),
                sellerInfo.getStoreName(),
                orderSubtotal,
                deliveryFee,
                discountAmount,
                finalAmount,
                ordersPreviewResponseItemGroups
        ));

        // 배송지
        List<OrdersPreviewResponseAvailableAddress> availableAddresses =
                new ArrayList<>();

        List<UserAddressSaved> savedAddresses =
                userAddressSavedRepository.findAllByUserInfo_Uuid(userUuid);

        for (UserAddressSaved savedAddress : savedAddresses) {
            availableAddresses.add(
                    new OrdersPreviewResponseAvailableAddress(
                            savedAddress.getAddressId(),
                            savedAddress.getAddress()
                    )
            );
        }

        // 결제수단
        List<OrdersPreviewResponseAvailablePayment> availablePayments =
                new ArrayList<>();

        List<UserPaymentSaved> savedPayments =
                userPaymentSavedRepository.findAllByUserInfo_Uuid(userUuid);

        for (UserPaymentSaved savedPayment : savedPayments) {
            availablePayments.add(
                    new OrdersPreviewResponseAvailablePayment(
                            savedPayment.getPaymentId(),
                            savedPayment.getPaymentEncryptedData()
                    )
            );
        }

        return new OrdersPreviewResponse(
                finalAmount,
                storeGroups,
                availableAddresses,
                availablePayments,
                List.of()
        );
    }

    // TODO[ORDER-COUPON-POLICY]:
    // V1: store-wide coupon is applied independently to every item in that store.
    // Later: apply a store-wide coupon only to the item that produces the largest discount within the store.
    @Transactional
    public CreateOrdersResponse newOrderBasket(
            UUID userUuid,
            List<String> couponCode,
            PurchaseType purchaseType,
            Long paymentId
    ) {
        // order를 만들고 basket을 삭제하기; order를 만들다 에러나면 롤백되지롱
        // check data
        if(purchaseType == null){throw new PurchaseFailException("Purchase Type Not Found");}
        // make userInfo and basket with items
        UserInfo userInfo = userInfoRepository.findById(userUuid).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        UserBasket userBasket = userBasketRepository.findByUserInfo_Uuid(userUuid).orElseThrow(() -> new EntityNotFoundException("Basket Not Found"));
        List<BasketItem> basketItems = basketItemRepository.findByUserBasket_BasketId(userBasket.getBasketId());
        if (basketItems.isEmpty()) {throw new EntityNotFoundException("Basket is empty");}
        // request 검증
        // purchaseType
                //        for (BasketItem basketItem : basketItems) {
                //            Item item = basketItem.getItem();
                //            int quantity = basketItem.getQuantity();
                //            int available = item.getAvailable();
                //
                //            // available == 0
                //            // available < quantity
                //            // subtractAvailable(quantity)
                //        }
        // Coupon 조회 -> error coupon occurs, only count error coupon code
        HashMap<UUID, Coupon> coupons = new HashMap<>();
        List<String> couponErrorNotifyList = new ArrayList<>();
        if (couponCode != null) {
            for (String code : couponCode) {
                if (code == null || code.isBlank()) {
                    continue;
                }
                if (couponErrorNotifyList.isEmpty() && code != null) {// && !code.isEmpty() 이건 필요없지않나?
                    Optional<Coupon> coupon = couponRepository.findById(code);
                    if (coupon.isPresent()) {
                        coupons.put(coupon.get().getSellerInfo().getStoreUuid(), coupon.get());
                    } else {
                        couponErrorNotifyList.add(code + " not found\n");
                    }
                } else if (!couponRepository.existsById(code)) {
                    couponErrorNotifyList.add(code + " not found\n");
                }
            }
        }
        if (!couponErrorNotifyList.isEmpty()) {
            throw new CouponNotFoundException(couponErrorNotifyList.toString());
        }
        // BasketItems 전체 재고 검증 + 차감 + 밑에 있는 것들
        // subtotal / discount / finalAmount calculation for individual store
        ArrayList<String> basketErrorNotifyList = new ArrayList<>();
        HashMap<Item, Integer> ordersItemQuantities = new HashMap<>();
//        HashMap<Item, BigDecimal> ordersItemPrices = new HashMap<>();
        HashMap<Item, BigDecimal> ordersDiscountPrices = new HashMap<>();
        HashMap<Item, Coupon> ordersAppliedCoupons = new HashMap<>();
        HashMap<SellerInfo, BigDecimal> totalPerSeller = new HashMap<>();
        for (BasketItem basketItem : basketItems) {
            Item item = basketItem.getItem();
            int quantity = basketItem.getQuantity();
            if(basketErrorNotifyList.isEmpty()){
                if (quantity <= item.getAvailable()) {
                    SellerInfo sellerInfo = item.getSellerInfo();
                    BigDecimal subTotal = item.getPrice().multiply(BigDecimal.valueOf(quantity));
                    Coupon coupon = coupons.get(sellerInfo.getStoreUuid());
                    BigDecimal discount = BigDecimal.ZERO;
                    if(coupon != null){
                        Item couponItem = coupon.getItem();
                        if (couponItem == null || couponItem.equals(item)) {
                            switch (coupon.getDiscountType()) {
                                case PRICE -> discount = coupon.getDiscountAmount();
                                case PERCENT -> discount = subTotal
                                        .multiply(coupon.getDiscountAmount()).divide(BigDecimal.valueOf(100));
                            }
                            if(discount.compareTo(coupon.getDiscountLimit())>0){
                                discount = coupon.getDiscountLimit();
                            }
                        }

                    }
                    if (!item.subtractAvailable(quantity)) {
                        basketErrorNotifyList.addLast("Item " + item.getItemCode() + " has stock error");
                        continue;
                    }
                    ordersItemQuantities.put(item, quantity);
                    if (discount.compareTo(BigDecimal.ZERO) > 0){
                        ordersDiscountPrices.put(item, discount);
                        ordersAppliedCoupons.put(item, coupon);
                        totalPerSeller.put(sellerInfo, totalPerSeller.getOrDefault(sellerInfo, BigDecimal.ZERO)
                                .add(subTotal).subtract(discount));
//                        ordersItemPrices.put(item, ordersItemPrices.getOrDefault(item, BigDecimal.ZERO)
//                                .add(subTotal).subtract(discount));
                    }
                    else{
                        totalPerSeller.put(sellerInfo, totalPerSeller.getOrDefault(sellerInfo, BigDecimal.ZERO).add(subTotal));
//                        ordersItemPrices.put(item, ordersItemPrices.getOrDefault(item, BigDecimal.ZERO).add(subTotal));
                    }
                } else {
                    basketErrorNotifyList.addLast(item.getItemName() + " has " + item.getAvailable() + " items, \n");
                }
            }
            else{
                if (quantity > item.getAvailable()) {
                    basketErrorNotifyList.addLast(item.getItemName() + " has " + item.getAvailable() + " items, \n");
                }
            }
        }
        if (!basketErrorNotifyList.isEmpty()) {
            throw new ItemOutOfStockException(basketErrorNotifyList.toString());
        }
        // 6. checkoutTotalAmount calc
        BigDecimal finalAmount =  BigDecimal.ZERO;
        for (BigDecimal price : totalPerSeller.values()){
            finalAmount = finalAmount.add(price);
        }
        //
        // 7. pay
        // TODO[PAYMENT-TX]:
        // Current payment service is a stub.
        // Real external payment cannot be rolled back by JPA transaction.
        // Introduce payment pending/confirmation + compensation/idempotency
        // when integrating an actual payment provider.
        switch (purchaseType) {
            case CARD -> {
                if(paymentId == null){
                    throw new PurchaseFailException("Payment Id is null");
                }
                if(!userPaymentService.payByCard(finalAmount, paymentId)){
                    throw new PurchaseFailException("Payment failed");
                }
                else break;
            }
            case BANK_BOOK -> {
                if(!userPaymentService.payByBankBook(finalAmount)){
                    throw new PurchaseFailException("Payment failed");
                }
                else break;
            }
            case MOBILE_CARRIER -> {
                if(!userPaymentService.payByMobileCarrier(finalAmount)){
                    throw new PurchaseFailException("Payment failed");
                }
                else break;
            }
            default -> throw new PurchaseFailException("Unknown purchase type");
        }

        // 8. Generate Checkout
        Checkout newCheckout = Checkout.create(userInfo, finalAmount, CheckoutStatus.CREATED);
        // 9. Generate OrderEntity for individual Stores
        HashMap<SellerInfo, OrderEntity> orderEntities = new HashMap<>();
        for (SellerInfo seller : totalPerSeller.keySet()) {
            OrderEntity newOrderEntity = OrderEntity.create(
                    newCheckout, userInfo, seller, OrderStatus.PAID,
                    null, null, null, null, null,
                    false,
                    null, null);
            orderEntities.put(seller, newOrderEntity);
        }
        //    + OrderItems
        HashMap<Item, OrderItem> orderItems = new HashMap<>();
        for (Item item : ordersItemQuantities.keySet()) {
            OrderItem newOrderItem = null;
            newOrderItem = OrderItem.create(
                    orderEntities.get(item.getSellerInfo()),
                    item,
                    ordersItemQuantities.get(item),
                    item.getPrice()
            );
//            OrderItem.create(
//                    orderEntities.get(item.getSellerInfo()), item, ordersItemQuantities.get(item),
//                    (item.getPrice().multiply(BigDecimal.valueOf(ordersItemQuantities.get(item)))
//                            .subtract(ordersDiscountPrices.getOrDefault(item, BigDecimal.ZERO))
//                            .divide(BigDecimal.valueOf(ordersItemQuantities.get(item)))));
            orderItems.put(item, newOrderItem);
        }
        //    + OrderCoupons(if necessary)
        // OrderCoupon이 OrderItem의 PK인 order_item_id를 참조하는 것을 고려 -> 그렇게 하기로 결정
        HashMap<Item, OrderCoupon> orderCoupons = new HashMap<>();
        for(Item appliedItem : ordersAppliedCoupons.keySet()){
            Coupon coupon = ordersAppliedCoupons.get(appliedItem);
            OrderCoupon newOrderCoupon = OrderCoupon.create(
                    orderItems.get(appliedItem),
                    coupon,
                    DiscountTypeSnapshot.valueOf(coupon.getDiscountType().name()),
                    coupon.getDiscountAmount(),
                    coupon.getDiscountLimit(),
                    ordersDiscountPrices.get(appliedItem)
            );
            orderCoupons.put(appliedItem, newOrderCoupon);
        }
        // 10. save
        checkoutRepository.save(newCheckout);
        for (OrderEntity orderEntity : orderEntities.values()) {
            orderRepository.save(orderEntity);
        }
        for (OrderItem orderItem : orderItems.values()) {
            orderItemRepository.save(orderItem);
        }
        for (OrderCoupon orderCoupon : orderCoupons.values()) {
            orderCouponRepository.save(orderCoupon);
        }
        // 11. delete BasketItems
        userBasketRepository.delete(userBasket);
        // 12. Return checkoutId + orderIds by CreateOrdersResponse
        ArrayList<Long>  orderIds = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntities.values()) {
            orderIds.add(orderEntity.getOrderId());
        }
        return new CreateOrdersResponse(newCheckout.getCheckoutId(), orderIds);
    }

    @Transactional
    public CreateOrdersResponse newOrderItem(
            UUID userUuid,
            long itemCode,
            int quantity,
            String couponCode,
            PurchaseType purchaseType,
            Long paymentId
    ) {
        // check data
        if(purchaseType == null){throw new PurchaseFailException("Purchase Type Not Found");}
        if(quantity <= 0){throw new InvalidQuantityException("Quantity must be greater than 0");}
        // make userInfo and item
        UserInfo userInfo = userInfoRepository.findById(userUuid).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Item item = itemRepository.findById(itemCode).orElseThrow(() ->
                new ItemNotFoundException("Item " + itemCode + " not found"));
        // check item availalbe
        int availableItem = item.getAvailable();
        if (availableItem == 0) {throw new ItemOutOfStockException("Item " + itemCode + " is out of stock");}
        if (availableItem < quantity) {throw new InsufficientStockException("Item " + itemCode + " has insufficient stock");}
        // remove quantity to buy
        //TODO 이 ArithmeticException은 나중에는 별도 도메인 예외로 바꾸기
        if(!item.subtractAvailable(quantity)){
            throw new ArithmeticException("Item " + itemCode + " has stock error");
        }
        Coupon coupon = null;
        if (couponCode != null) {coupon = couponRepository.findById(couponCode).orElseThrow(() ->
                new CouponNotFoundException("Coupon " + couponCode + " not found"));}
        // decide price and fee
        BigDecimal unitPrice = item.getPrice();
        BigDecimal orderSubtotal =  unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal deliveryFee =  BigDecimal.ZERO;
        // decide finalAmount and discountAmount by coupon
        BigDecimal discountAmount = null;
        BigDecimal finalAmount = null;
        if (coupon != null) {
            if (!coupon.getSellerInfo().getStoreUuid()
                    .equals(item.getSellerInfo().getStoreUuid())) {
                throw new CouponNotMatchException("Coupon not match");
            }
            if (coupon.getItem() != null &&
                    !coupon.getItem().getItemCode().equals(item.getItemCode())) {
                throw new CouponNotMatchException("Coupon not match");
            }
            switch (coupon.getDiscountType()) {
                case PRICE -> discountAmount = coupon.getDiscountAmount();
                case PERCENT -> discountAmount = orderSubtotal
                        .multiply(coupon.getDiscountAmount()).divide(BigDecimal.valueOf(100));
            }
            if(discountAmount.compareTo(coupon.getDiscountLimit())>0){
                discountAmount = coupon.getDiscountLimit();
            }
            finalAmount = orderSubtotal.add(deliveryFee).subtract(discountAmount);
        }
        else {
            discountAmount = BigDecimal.ZERO;
            finalAmount = orderSubtotal.add(deliveryFee);
        }

        // TODO[PAYMENT-TX]:
        // Current payment service is a stub.
        // Real external payment cannot be rolled back by JPA transaction.
        // Introduce payment pending/confirmation + compensation/idempotency
        // when integrating an actual payment provider.
        switch (purchaseType) {
            case CARD -> {
                if(paymentId == null){
                    throw new PurchaseFailException("Payment Id is null");
                }
                if(!userPaymentService.payByCard(finalAmount, paymentId)){
                    throw new PurchaseFailException("Payment failed");
                }
                else break;
            }
            case BANK_BOOK -> {
                if(!userPaymentService.payByBankBook(finalAmount)){
                    throw new PurchaseFailException("Payment failed");
                }
                else break;
            }
            case MOBILE_CARRIER -> {
                if(!userPaymentService.payByMobileCarrier(finalAmount)){
                    throw new PurchaseFailException("Payment failed");
                }
                else break;
            }
            default -> throw new PurchaseFailException("Unknown purchase type");
        }
        SellerInfo sellerInfo = item.getSellerInfo();
        // create related entity
        Checkout newCheckout = Checkout.create(
                userInfo,
                finalAmount,
                CheckoutStatus.CREATED
        );
        OrderEntity newOrderEntity = OrderEntity.create(
                newCheckout,
                userInfo,
                sellerInfo,
                OrderStatus.PAID,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null
        );
        // OrderEvent newOrderEvent = OrderEvent.create();// 이건 만들 필요가 없네...
        OrderItem orderItem = OrderItem.create(
                newOrderEntity,
                item,
                quantity,
                unitPrice
        );
        // coupon이 null이 아닌 경우 만들기 -> 추후 coupon 사용여부에 따라 DB에 데이터 입력여부 결정
        OrderCoupon newOrderCoupon = null;
        if(coupon != null) {
            newOrderCoupon = OrderCoupon.create(
                    orderItem,
                    coupon,
                    DiscountTypeSnapshot.valueOf(coupon.getDiscountType().name()),
                    coupon.getDiscountAmount(),
                    coupon.getDiscountLimit(),
                    discountAmount
            );
        }
        // save created entities
        checkoutRepository.save(newCheckout);
        orderRepository.save(newOrderEntity);
        orderItemRepository.save(orderItem);
        if(newOrderCoupon != null) {
            orderCouponRepository.save(newOrderCoupon);
        }
        // save subtracted quantity //itemRepository.save(item); -> dirty changing
        //TODO 여기에 구매파트를 놓는게 더 합리적일지도...
        //return CreateOrdersResponse
        ArrayList<Long>  orderIds = new ArrayList<>();
        orderIds.add(newOrderEntity.getOrderId());
        return new CreateOrdersResponse(newCheckout.getCheckoutId(), orderIds); // ID를 이렇게 받는게 맞나...
    }
}
