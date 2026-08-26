package com.seyoon.portfolio.service;

import com.seyoon.portfolio.dto.response.*;
import com.seyoon.portfolio.dto.response.trashbin.ItemOrdersPreviewResponse;
import com.seyoon.portfolio.entity.*;
import com.seyoon.portfolio.exception.CouponNotFoundException;
import com.seyoon.portfolio.exception.CouponNotMatchException;
import com.seyoon.portfolio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class UserOrderService {

    private final UserBasketRepository userBasketRepository;
    private final BasketItemRepository basketItemRepository;
    private final CouponRepository couponRepository;
    private final ItemRepository itemRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserAddressSavedRepository userAddressSavedRepository;
    private final UserPaymentSavedRepository userPaymentSavedRepository;

    public UserOrderService(UserBasketRepository userBasketRepository,
                            BasketItemRepository basketItemRepository,
                            CouponRepository couponRepository,
                            ItemRepository itemRepository,
                            UserInfoRepository userInfoRepository,
                            UserAddressSavedRepository userAddressSavedRepository,
                            UserPaymentSavedRepository userPaymentSavedRepository) {
        this.userBasketRepository = userBasketRepository;
        this.basketItemRepository = basketItemRepository;
        this.couponRepository = couponRepository;
        this.itemRepository = itemRepository;
        this.userInfoRepository = userInfoRepository;
        this.userAddressSavedRepository = userAddressSavedRepository;
        this.userPaymentSavedRepository = userPaymentSavedRepository;
    }

    //Preview에서는 Order를 만들면 안 됨 -> @Transactional(readOnly = true) 붙이기

//    @Transactional(readOnly = true)
//    public OrdersPreviewResponse newPreviewBasket(UUID userUuid, String couponCode) {
//        Optional<UserBasket> userBasket = userBasketRepository.findByUserInfo_Uuid(userUuid);
//        if (userBasket.isPresent()) {
//
//            // BasketItem 조회
//            Long basketId = userBasket.get().getBasketId();
//            List<BasketItem> basketItems = basketItemRepository.findByUserBasket_BasketId(basketId);
//
//            // BasketItem sorting
//            basketItems.sort(Comparator.comparing(BasketItem::getItemStoreUUID));//이거 store까지만 불러오면 sorting 안되려나?
//
//            // DTO 조립
//            List<OrdersPreviewResponseStoreGroup> storeGroups = new ArrayList<>();
//            List<OrdersPreviewResponseAvailableAddress> availableAddresses = new ArrayList<>();
//            List<OrdersPreviewResponseAvailablePayment> availablePayments = new ArrayList<>();
//            List<String> warnings = new ArrayList<>();
//
//            // generate storeGroups
//            BigDecimal totalAmount = BigDecimal.ZERO;
//            BigDecimal orderSubtotal = BigDecimal.ZERO;
//            BigDecimal deliveryFee = BigDecimal.valueOf(5); //BigDecimal.ZERO; 지금 배송비는 DB에 없다????
//            BigDecimal discountAmount = BigDecimal.ZERO;
//            BigDecimal finalAmount = BigDecimal.ZERO;
//
//            for (BasketItem basketItem : basketItems) {
//                Item item = basketItem.getItem();
//                SellerInfo sellerInfo = item.getSellerInfo();
//                String mainImage = item.getMainImages().isEmpty() ? null : item.getMainImages().getFirst();
//                BigDecimal currentPrice = item.getPrice();
//                int quantity = basketItem.getQuantity();
//                int available =  item.getAvailable();
//                BigDecimal lineTotal = currentPrice.multiply(BigDecimal.valueOf(quantity));
//                //boolean sellable = available > 0 && quantity <= available;
//                //if(sellable){}
//
//                if(storeGroups.getLast().storeUuid().equals(sellerInfo.getStoreUuid())){
//                    storeGroups.getLast().items().addLast(new OrdersPreviewResponseItemGroup(
//                            item.getItemCode(), item.getItemName(), quantity, currentPrice, lineTotal, mainImage));
//                    orderSubtotal = orderSubtotal.add(lineTotal);
//                }
//                else if(storeGroups.isEmpty()){
//                    storeGroups.addLast(new OrdersPreviewResponseStoreGroup(sellerInfo.getStoreUuid(), sellerInfo.getStoreName(),
//                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
//                            new ArrayList<OrdersPreviewResponseItemGroup>()));
//                    storeGroups.getLast().items().addLast(new OrdersPreviewResponseItemGroup(
//                            item.getItemCode(), item.getItemName(), quantity, currentPrice, lineTotal, mainImage));
//                }
//                else{//storeGroup은 not empty, 새 store과 item 추가
//                    //store finishing
//                    Coupon coupon = couponRepository.getReferenceById(couponCode);
//                    switch (coupon.getDiscountType()){
//                        case PRICE -> discountAmount = coupon.getDiscountAmount();
//                        case PERCENT -> discountAmount = orderSubtotal.multiply(coupon.getDiscountAmount()).divide(BigDecimal.valueOf(100));
//                    }
//                    if(discountAmount.max(coupon.getDiscountLimit()).equals(discountAmount)){
//                        discountAmount = coupon.getDiscountLimit();
//                    }
//                    finalAmount = orderSubtotal.subtract(discountAmount);
//
//                    // total amount
//                    totalAmount = totalAmount.add(finalAmount);
//
//                    // 이게 문제네... record라서; record 생성을 늦춰야햐나...
//                    storeGroups.getLast().orderSubtotal() = orderSubtotal;
//                    storeGroups.getLast().deliveryFee() = deliveryFee;
//                    storeGroups.getLast().discountAmount() = discountAmount;
//                    storeGroups.getLast().finalAmount() = finalAmount;
//
//                    // orderSubtotal, deliveryFee, discountAmount, finalAmount 재정비
//                    orderSubtotal = BigDecimal.ZERO;
//                    //deliveryFee = BigDecimal.ZERO;
//                    discountAmount = BigDecimal.ZERO;
//                    finalAmount = BigDecimal.ZERO;
//                    //새 store과 item 추가
//                    storeGroups.addLast(new OrdersPreviewResponseStoreGroup(sellerInfo.getStoreUuid(), sellerInfo.getStoreName(),
//                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
//                            new ArrayList<OrdersPreviewResponseItemGroup>()));
//                    storeGroups.getLast().items().addLast(new OrdersPreviewResponseItemGroup(
//                            item.getItemCode(), item.getItemName(), quantity, currentPrice, lineTotal, mainImage));
//                }//마지막인 경우에서 edgecase 존재ㅣ BigDecimal 잘 setting 하게 edge-case에 맞는 data 삽입 필요
//            }
//
//            // generating available addresses
//            //List<OrdersPreviewResponseAvailableAddress> availableAddresses = new ArrayList<>();
//            List<UserAddressSaved> userAddressSaves = userAddressSavedRepository.findAllByUserInfo_Uuid(userUuid);
//            for(UserAddressSaved userAddressSaved : userAddressSaves){
//                availableAddresses.add(new OrdersPreviewResponseAvailableAddress(
//                        userAddressSaved.getAddressId(), userAddressSaved.getAddress()));
//            }
//            // generating avalable payments
//            //List<OrdersPreviewResponseAvailablePayment> availablePayments = new ArrayList<>();
//            List<UserPaymentSaved> userPaymentSaves = userPaymentSavedRepository.findAllByUserInfo_Uuid(userUuid);
//            for(UserPaymentSaved userPaymentSaved : userPaymentSaves){
//                availablePayments.add(new OrdersPreviewResponseAvailablePayment(
//                        userPaymentSaved.getPaymentId(), userPaymentSaved.getPaymentEncryptedData()
//                ));
//            }
//            // warnings record;
//            //return
//            return new OrdersPreviewResponse(totalAmount, storeGroups, availableAddresses, availablePayments, warnings);
//        }
//        else{return new OrdersPreviewResponse(BigDecimal.ZERO, List.of(), List.of(), List.of(), List.of());}
//    }// asc로 sorting하는 알고리즘 써서 만들것

    @Transactional(readOnly = true)
    public OrdersPreviewResponse newPreviewBasket(UUID userUuid, String couponCode) {
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
    public ItemOrdersPreviewResponse newPreviewItem(BasketItem basketItem) {}

    public CreateOrdersResponse newOrderBasket() {}

    public CreateOrdersResponse newOrderItem() {}
}
