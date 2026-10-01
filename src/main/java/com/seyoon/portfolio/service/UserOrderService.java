package com.seyoon.portfolio.service;

import com.seyoon.portfolio.dto.PurchaseType;
import com.seyoon.portfolio.dto.response.*;
import com.seyoon.portfolio.entity.*;
import com.seyoon.portfolio.entity.type.CheckoutStatus;
import com.seyoon.portfolio.entity.type.DiscountTypeSnapshot;
import com.seyoon.portfolio.entity.type.OrderStatus;
import com.seyoon.portfolio.exception.*;
import com.seyoon.portfolio.repository.*;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static com.seyoon.portfolio.dto.PurchaseType.CARD;

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
    //TODO calculatePricing()사용하는 방향으로 바꾸기
    @Transactional(readOnly = true)
    public OrdersPreviewResponse newPreviewBasket(
            UUID userUuid,
            List<String> couponCodes
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

        List<BasketItem> basketItems = basketItemRepository.findAllByUserBasket_BasketId(userBasket.getBasketId());
//        // storeUuid ASC
//        basketItems.sort(Comparator.comparing(BasketItem::getItemStoreUUID));

        //itemId-coupon mapping
        Map<Long, Coupon> couponByItemCode = new HashMap<>(); // <ItemId, Coupon>
        if (couponCodes != null) {
            for (String couponCode : couponCodes) { // TODO 나중에 couponRepository.remove(item.getItemCode())로 최적화
                Coupon coupon = returnAvailableCoupon(couponCode);
                if (coupon == null) {continue;}
                Long itemCode = coupon.getItem().getItemCode();
                if (couponByItemCode.containsKey(itemCode)) {
                    throw new CouponNotMatchException("Item " + itemCode + " can use only one coupon");
                }
                couponByItemCode.put(itemCode, coupon);
            }
        }
        // store-item mapping
        Map<UUID, List<BasketItem>> basketItemsByStores = new HashMap<>();
        for (BasketItem basketItem : basketItems) {
            UUID storeUUID = basketItem.getItemStoreUUID();
            basketItemsByStores.putIfAbsent(storeUUID, new ArrayList<>());
            basketItemsByStores.get(storeUUID).add(basketItem);
        }
        // calculate and make responseDTO
        //List<String> warnings = new ArrayList<>();
        List<OrdersPreviewResponseStoreGroup> storeGroups = new ArrayList<>();

        BigDecimal checkoutTotalAmount = BigDecimal.ZERO;

        for (List<BasketItem> basketItemsByStore : basketItemsByStores.values()) {
            UUID currentStoreUuid = basketItemsByStore.getFirst().getItemStoreUUID();
            String currentStoreName = basketItemsByStore.getFirst().getItem().getSellerInfo().getStoreName();
            BigDecimal orderSubtotal = BigDecimal.ZERO;
            BigDecimal discountAmount  = BigDecimal.ZERO;

            List<OrdersPreviewResponseItemGroup> currentItems = new ArrayList<>();

            for (BasketItem basketItem : basketItemsByStore) {

                int quantity = basketItem.getQuantity();
                Item item = basketItem.getItem();
                validatePreviewAvailableItem(item, quantity);

                BigDecimal unitPrice = item.getPrice();
                // apply coupon discount to line total
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                orderSubtotal = orderSubtotal.add(lineTotal);
                Coupon coupon = couponByItemCode.remove(item.getItemCode());
                if (coupon != null) {
                    BigDecimal itemDiscount = returnDiscountAmount(coupon, item, lineTotal);
                    discountAmount = discountAmount.add(itemDiscount);
                }
                String mainImage = item.getMainImages().isEmpty()
                        ? null
                        : item.getMainImages().getFirst();

                currentItems.add(
                    new OrdersPreviewResponseItemGroup(
                        item.getItemCode(), item.getItemName(), quantity,unitPrice, lineTotal, mainImage
                    )
                );
            }
            BigDecimal finalAmount =  orderSubtotal.subtract(discountAmount);
            storeGroups.add(
                new OrdersPreviewResponseStoreGroup(
                    currentStoreUuid, currentStoreName, orderSubtotal, BigDecimal.ZERO,
                        discountAmount, finalAmount, currentItems
                )
            );
            checkoutTotalAmount = checkoutTotalAmount.add(finalAmount);
        }
        if(!couponByItemCode.isEmpty()) {
            throw new CouponNotMatchException("unused coupon left");
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
                checkoutTotalAmount, storeGroups, availableAddresses, availablePayments, List.of()
        );
    }


    @Transactional(readOnly = true)
    public OrdersPreviewResponse newPreviewItem(
            UUID userUuid,
            Long itemCode,
            int quantity,
            String couponCode
    ) {
        Item item = previewAvailableItem(itemCode, quantity);

        Coupon coupon = returnAvailableCoupon(couponCode);

        BigDecimal unitPrice = item.getPrice();
        BigDecimal orderSubtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal deliveryFee =  BigDecimal.ZERO;

        BigDecimal discountAmount = null;
        BigDecimal finalAmount = null;

        if(coupon != null) {
            discountAmount = returnDiscountAmount(coupon, item, orderSubtotal);
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


    @Transactional
    public CreateOrdersResponse newOrderBasket(
            UUID userUuid,
            Long addressId,
            List<String> couponCodes,
            PurchaseType purchaseType,
            Long paymentId
    ) {
        // 1. purchaseType 검증
//        switch (purchaseType) {
//            case MOBILE_CARRIER, CARD, BANK_BOOK:
//                break;
//            default:
//                throw new IllegalArgumentException("Invalid purchase type");
//        }
        if (purchaseType == null) {
            throw new PurchaseFailException("Purchase Type Not Found");
        }
        // 2. User 조회
        UserInfo userInfo = userInfoRepository.findById(userUuid).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        // 3. Basket + BasketItems 조회
        UserBasket userBasket = userBasketRepository.findByUserInfo_Uuid(userUuid)
                .orElse(null);

        if (userBasket == null) {
            throw new EntityNotFoundException("Your basket is empty");
        }
        List<BasketItem> basketItems = basketItemRepository.findAllByUserBasket_BasketId(userBasket.getBasketId());
        if (basketItems.isEmpty()) {
            throw new EntityNotFoundException("Basket is empty");
        }
        // 4. Address ownership 검증
        UserAddressSaved savedAddress =
                userAddressSavedRepository
                        .findByAddressIdAndUserInfo_Uuid(addressId, userUuid)
                        .orElseThrow(() ->
                                new EntityNotFoundException("Address not found"));
        String userAddressSnapshot = savedAddress.getAddress();
//        Optional<UserAddressSaved> userAddressSaved = userAddressSavedRepository.findByAddressIdAndUserInfo_Uuid(addressId, userUuid);
//        if (userAddressSaved.isEmpty() || !userAddressSaved.get().getUserInfo().getUuid().equals(userUuid)) {
//            throw new EntityNotFoundException("Your address doesn't exist");
//        }
//        String userAddressSnapshot = userAddressSaved.get().getAddress();
        // 5. couponCodes
        // → Map<Long, Coupon> couponByItemCode
        Map<Long, Coupon> couponByItemCode = new HashMap<>();
        // → 중복 item coupon 방지
        if (couponCodes != null) {
            for (String couponCode : couponCodes) {
                Coupon coupon = returnAvailableCoupon(couponCode);
                if (coupon == null) {continue;}
                Long itemCode = coupon.getItem().getItemCode();
                if (couponByItemCode.containsKey(itemCode)) {
                    throw new CouponNotMatchException("Item " + itemCode + " can use only one coupon");
                }
                couponByItemCode.put(itemCode, coupon);
            }
        }
        // 6. BasketItems를 itemCode 순으로 정렬 (atomic stock update lock 순서 대비) ? 차라리 storeUUID순이 낫지 않나? // storeUuid ASC
//        basketItems.sort(Comparator.comparing(BasketItem::getItemStoreUUID));
        basketItems.sort(
                Comparator.comparing(
                        basketItem -> basketItem.getItem().getItemCode()
                )
        );
        // 7. List<OrderLineCalculation> lines; Map<SellerInfo, BigDecimal> totalPerSeller
        List<OrderLineCalculation> lines = new ArrayList<>();
        Map<SellerInfo, BigDecimal> totalPerSeller = new  HashMap<>();
        // 8. BasketItem loop
        for (BasketItem basketItem : basketItems) {
            // - Item / quantity
            Item item = basketItem.getItem();
            Long itemCode = item.getItemCode();
            int quantity = basketItem.getQuantity();
            if (quantity <= 0) {throw new InvalidQuantityException("Quantity must be bigger than 0");}
            // - Coupon 가져오기
            Coupon coupon = couponByItemCode.remove(itemCode);
            // - calculatePricing()
            PricingResult pricing = calculatePricing(item, quantity, coupon);
            // - atomic stock update
            int updatedRows = itemRepository.decreaseAvailable(itemCode, quantity);
            if (updatedRows == 0) {
                throw new InsufficientStockException("Insufficient Stock");
            }
            // - lines.add(...)
            lines.add(new OrderLineCalculation(item, quantity, coupon, pricing));
            // - totalPerSeller.merge(...)
            SellerInfo sellerInfo = item.getSellerInfo();
            totalPerSeller.put(sellerInfo, totalPerSeller.getOrDefault(sellerInfo, BigDecimal.ZERO).add(pricing.finalAmount()));
        }
        // 9. 사용되지 않은 Coupon 있으면 reject
        if(!couponByItemCode.isEmpty()) {throw new CouponNotMatchException("Some Coupon Not Matches");}
        // 10. checkoutTotal 계산
        BigDecimal finalAmount = BigDecimal.ZERO;
        for (BigDecimal pricePerSeller : totalPerSeller.values()){finalAmount = finalAmount.add(pricePerSeller);}
        // 12. Checkout 생성
        Checkout checkout = Checkout.create(userInfo, finalAmount, CheckoutStatus.CREATED);
        // 13. Seller별 OrderEntity 생성
        Map<UUID, OrderEntity> orderEntities = new HashMap<>();
        for (SellerInfo sellerInfo : totalPerSeller.keySet()) {
            orderEntities.put(sellerInfo.getStoreUuid(),
                    OrderEntity.create(checkout, userInfo, sellerInfo, OrderStatus.PAID, null, null,
                            null, null, null, false, null,
                            null, userAddressSnapshot, userAddressSnapshot));
        }
        // 14. lines 다시 순회
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderCoupon> orderCoupons = new ArrayList<>();
        for(OrderLineCalculation line : lines) {
            // - OrderItem 생성
            Item item = line.item();
            OrderItem orderItem = OrderItem.create(
                    orderEntities.get(item.getSellerInfo().getStoreUuid()), item,
                    line.quantity(), line.pricing.unitPrice(),item.getItemName());
            orderItems.add(orderItem);
            // - coupon != null이면 OrderCoupon 생성
            if(line.coupon() != null) {
                Coupon coupon = line.coupon();
                PricingResult pricing = line.pricing();
                orderCoupons.add(OrderCoupon.create(orderItem, coupon, DiscountTypeSnapshot.valueOf(coupon.getDiscountType().name()),
                        coupon.getDiscountAmount(), coupon.getDiscountLimit(), pricing.discountAmount()));
            }
        }
        // 15. save (checkout, orderEntities.values(), orderItems)
        checkoutRepository.save(checkout);
        for (OrderEntity orderEntity : orderEntities.values()) {
            orderRepository.save(orderEntity);
        }
        for (OrderItem orderItem : orderItems) {
            orderItemRepository.save(orderItem);
        }
        for (OrderCoupon orderCoupon : orderCoupons) {
            orderCouponRepository.save(orderCoupon);
        }
        // 16. Basket 삭제
        basketItemRepository.deleteAllByUserBasket_BasketId(userBasket.getBasketId());
        userBasketRepository.delete(userBasket);
        // 11. payment
        if(!purchase(finalAmount, purchaseType, paymentId)){
            throw new PurchaseFailException("Purchase Failed");
        }
        // 17. CreateOrdersResponse
        List<Long> orderIds = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntities.values()) {
            orderIds.add(orderEntity.getOrderId());
        }
        return new CreateOrdersResponse(checkout.getCheckoutId(), orderIds);
    }



    @Transactional
    public CreateOrdersResponse newOrderItem(
            UUID userUuid,
            long itemCode,
            int quantity,
            String couponCode,
            Long addressId,
            PurchaseType purchaseType,
            Long paymentId
    ) {
        // check data
        if(purchaseType == null){throw new PurchaseFailException("Purchase Type Not Found");}
        if(quantity <= 0){throw new InvalidQuantityException("Quantity must be greater than 0");}

        // make userInfo and item
        UserInfo userInfo = userInfoRepository.findById(userUuid).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Item item = itemRepository.findById(itemCode).orElseThrow(() ->
                new ItemNotFoundException("Item " + itemCode + " not found"));// previewAvailableItem으로 끝내면 안되나? 저 함수는 왜 preview용이지?
        // check item availalbe -> replaced by atomic update in ItemRepository
        //TODO 이 ArithmeticException은 나중에는 별도 도메인 예외로 바꾸기
        int updatedRows = itemRepository.decreaseAvailable(itemCode, quantity);
        if (updatedRows == 0) {
            throw new InsufficientStockException("Insufficient Stock");
        }
//        validatePreviewAvailableItem(item, quantity);
//        if(!item.subtractAvailable(quantity)){
//            throw new ArithmeticException("Item " + itemCode + " has stock error");
//        }
        Coupon coupon = returnAvailableCoupon(couponCode);
        // decide price and fee
        PricingResult pricingResult = calculatePricing(item, quantity, coupon);
//        BigDecimal unitPrice = item.getPrice();
//        BigDecimal orderSubtotal =  unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal deliveryFee =  BigDecimal.ZERO;
//        BigDecimal discountAmount = null;
//        BigDecimal finalAmount = null;
        UserAddressSaved savedAddress =
                userAddressSavedRepository
                        .findByAddressIdAndUserInfo_Uuid(addressId, userUuid)
                        .orElseThrow(() ->
                                new EntityNotFoundException("Address not found"));

        String shippingAddressSnapshot =
                savedAddress.getAddress();
        // decide finalAmount and discountAmount by coupon
//        if(coupon!=null) {
//            discountAmount = returnDiscountAmount(coupon, item, orderSubtotal);
//            finalAmount = orderSubtotal.add(deliveryFee).subtract(discountAmount);
//        }
//        else {
//            discountAmount = BigDecimal.ZERO;
//            finalAmount = orderSubtotal.add(deliveryFee);
//        }
        // purchase 자리였으나 이사감
        SellerInfo sellerInfo = item.getSellerInfo();
        // create related entity
        Checkout newCheckout = Checkout.create(
                userInfo,
                pricingResult.finalAmount(),
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
                null,
                shippingAddressSnapshot,
                shippingAddressSnapshot
        );
        // OrderEvent newOrderEvent = OrderEvent.create();// 이건 만들 필요가 없네...
        OrderItem orderItem = OrderItem.create(
                newOrderEntity,
                item,
                quantity,
                pricingResult.unitPrice(),
                item.getItemName()
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
                    pricingResult.discountAmount()
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
        //TODO 여기에 구매파트를 놓는게 더 합리적일 것 같아서 일단 옮김
        if(!purchase(pricingResult.finalAmount(), purchaseType, paymentId)){
            throw new PurchaseFailException("Purchase Failed");
        }
        //return CreateOrdersResponse
        ArrayList<Long>  orderIds = new ArrayList<>();
        orderIds.add(newOrderEntity.getOrderId());
        return new CreateOrdersResponse(newCheckout.getCheckoutId(), orderIds); // ID를 이렇게 받는게 맞나...
    }



    private boolean purchase(BigDecimal finalAmount, @NonNull PurchaseType purchaseType, Long paymentId) {
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
                else return true;
            }
            case BANK_BOOK -> {
                if(!userPaymentService.payByBankBook(finalAmount)){
                    throw new PurchaseFailException("Payment failed");
                }
                else return true;
            }
            case MOBILE_CARRIER -> {
                if(!userPaymentService.payByMobileCarrier(finalAmount)){
                    throw new PurchaseFailException("Payment failed");
                }
                else return true;
            }
            default -> throw new PurchaseFailException("Unknown purchase type");
        }
    }

    private void validatePreviewAvailableItem(Item item, int quantity) {
        if(quantity <= 0){throw new InvalidQuantityException("Quantity must be greater than 0");}
        if (item.getAvailable() == 0) {throw new ItemOutOfStockException("Item " + item.getItemCode() + " is out of stock");}
        if (item.getAvailable() < quantity) {throw new InsufficientStockException("Item " + item.getItemCode() + " has insufficient stock");}
    }

    private Item previewAvailableItem(Long itemCode, int quantity) {
        if(quantity <= 0){throw new InvalidQuantityException("Quantity must be greater than 0");}
        Item item = itemRepository.findById(itemCode).orElseThrow(() ->
                new ItemNotFoundException("Item " + itemCode + " not found"));
        if (item.getAvailable() == 0) {throw new ItemOutOfStockException("Item " + itemCode + " is out of stock");}
        if (item.getAvailable() < quantity) {throw new InsufficientStockException("Item " + itemCode + " has insufficient stock");}
        return item;
    }

    private Coupon returnAvailableCoupon(String couponCode) {
        if(couponCode==null || couponCode.isBlank()){return null;}
        Coupon coupon = couponRepository.findById(couponCode)
                .orElseThrow(() -> new CouponNotFoundException("Coupon " + couponCode + " not found"));
        if(coupon.getDueDate().isBefore(OffsetDateTime.now())) {
            throw new CouponNotMatchException("Coupon " + couponCode + " is expired");
        }
        return coupon;
    }

    private BigDecimal returnDiscountAmount(Coupon coupon, Item item, BigDecimal orderSubtotal) {
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (!coupon.getItem().getItemCode().equals(item.getItemCode())) {
            throw new CouponNotMatchException("Coupon not match");
        }
        switch (coupon.getDiscountType()) {
            case PRICE -> discountAmount = coupon.getDiscountAmount();
            case PERCENT -> discountAmount = orderSubtotal
                    .multiply(coupon.getDiscountAmount()).divide(BigDecimal.valueOf(100));
        }
        if(coupon.getDiscountLimit() != null && discountAmount.compareTo(coupon.getDiscountLimit())>0){
            discountAmount = coupon.getDiscountLimit();
        }
        if(discountAmount.compareTo(orderSubtotal) > 0){discountAmount = orderSubtotal;}
        return discountAmount;
    }

    private PricingResult calculatePricing(Item item, int quantity, Coupon coupon) {
        BigDecimal unitPrice = item.getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        BigDecimal discountAmount = coupon == null
                ? BigDecimal.ZERO
                : returnDiscountAmount(coupon, item, subtotal);

        BigDecimal finalAmount = subtotal.subtract(discountAmount);

        return new PricingResult(
                unitPrice,
                subtotal,
                discountAmount,
                finalAmount
        );
    }

    private record PricingResult(
            BigDecimal unitPrice,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            BigDecimal finalAmount
    ) {}

    private record OrderLineCalculation(
            Item item,
            int quantity,
            Coupon coupon,
            PricingResult pricing
    ) {}
}
