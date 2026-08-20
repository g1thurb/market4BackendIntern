package com.seyoon.portfolio.service;

import com.seyoon.portfolio.dto.response.BasketGetResponse;
import com.seyoon.portfolio.dto.response.BasketItemResponse;
import com.seyoon.portfolio.entity.BasketItem;
import com.seyoon.portfolio.entity.Item;
import com.seyoon.portfolio.entity.SellerInfo;
import com.seyoon.portfolio.entity.UserBasket;
import com.seyoon.portfolio.exception.InsufficientStockException;
import com.seyoon.portfolio.exception.InvalidQuantityException;
import com.seyoon.portfolio.exception.ItemOutOfStockException;
import com.seyoon.portfolio.repository.BasketItemRepository;
import com.seyoon.portfolio.repository.ItemRepository;
import com.seyoon.portfolio.repository.UserBasketRepository;
import com.seyoon.portfolio.repository.UserInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class UserBasketService {

    private final UserBasketRepository userBasketRepository;
    private final BasketItemRepository basketItemRepository;
    private final ItemRepository itemRepository;
    private final UserInfoRepository userInfoRepository;

    public UserBasketService(UserBasketRepository userBasketRepository, BasketItemRepository basketItemRepository,
                             ItemRepository itemRepository, UserInfoRepository userInfoRepository) {
        this.userBasketRepository = userBasketRepository;
        this.basketItemRepository = basketItemRepository;
        this.itemRepository = itemRepository;
        this.userInfoRepository = userInfoRepository;
    }

    @Transactional(readOnly = true)
    public BasketGetResponse getBasket(UUID userUuid) {
        // 장바구니 조회
        Optional<UserBasket> userBasket = userBasketRepository.findByUserInfo_Uuid(userUuid);
        if (userBasket.isPresent()) {
            // BasketItem 조회
            Long basketId = userBasket.get().getBasketId();
            List<BasketItem> basketItems = basketItemRepository.findByUserBasket_BasketId(basketId);
            // DTO 조립
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<BasketItemResponse> basketItemResponses = new ArrayList<>();
            for (BasketItem basketItem : basketItems) {
                Item item = basketItem.getItem();
                SellerInfo sellerInfo = item.getSellerInfo();
                String mainImage = item.getMainImages().isEmpty() ? null : item.getMainImages().getFirst();
                BigDecimal currentPrice = item.getPrice();
                int quantity = basketItem.getQuantity();
                int available =  item.getAvailable();
                BigDecimal lineTotal = currentPrice.multiply(BigDecimal.valueOf(quantity));
                boolean sellable = available > 0 && quantity <= available;
                if(sellable){totalAmount = totalAmount.add(lineTotal);}
                basketItemResponses.add(
                        new BasketItemResponse(basketItem.getBasketItemId(), item.getItemCode(), sellerInfo.getStoreUuid(),
                                sellerInfo.getStoreName(), item.getItemName(), mainImage, currentPrice,
                                quantity, available, lineTotal, sellable)
                );
            }
            // 반환
            return new BasketGetResponse(basketId, basketItemResponses, totalAmount);
        }
        else{return new BasketGetResponse(null, List.of(), BigDecimal.ZERO);}
    }

    @Transactional
    public void addItem(UUID userUuid, Long itemCode, int quantity) {
        if(quantity <= 0){throw new InvalidQuantityException("Quantity must be greater than zero");}
        Item item = itemRepository.findById(itemCode).orElseThrow(() -> new EntityNotFoundException("Item not found"));
        int available = item.getAvailable();
        if(available == 0){throw new ItemOutOfStockException("Not enough available items");}
        if(available < quantity){throw new InsufficientStockException("Quantity exceeded");}
        Optional<UserBasket> userBasket = userBasketRepository.findByUserInfo_Uuid(userUuid);
        if (userBasket.isPresent()) {
            Optional<BasketItem> basketItemOptional = basketItemRepository.findByUserBasketAndItem( userBasket.get(), item);
            if (basketItemOptional.isPresent()) {
                BasketItem basketItem = basketItemOptional.get();
                if (available < basketItem.getQuantity() + quantity) {throw new InsufficientStockException("Quantity exceeded");}
                basketItem.changeQuantity(basketItem.getQuantity() + quantity);
            }
            else {
                basketItemRepository.save(BasketItem.create(userBasket.get(), item, quantity));
            }
        }
        else {
            UserBasket newUserBasket = UserBasket.create(userInfoRepository.findById(userUuid)
                    .orElseThrow(() -> new EntityNotFoundException("User not found")));
            userBasketRepository.save(newUserBasket);
            basketItemRepository.save(BasketItem.create(newUserBasket, item, quantity));
        }
    }

    @Transactional
    public void changeQuantity(UUID userUuid, Long itemCode, int quantity) {
        // 수량 변경
        if(quantity < 0){throw new InvalidQuantityException("Quantity must be zero or greater");}
        else if (quantity == 0) {removeItem(userUuid, itemCode);return;}
        Item item = itemRepository.findById(itemCode).orElseThrow(() -> new EntityNotFoundException("Item not found"));
        if(item.getAvailable() == 0){throw new ItemOutOfStockException("Nothing available");}
        if(item.getAvailable() < quantity){throw new InsufficientStockException("Quantity exceeded");}
        Optional<UserBasket> userBasket = userBasketRepository.findByUserInfo_Uuid(userUuid);
        if (userBasket.isPresent()) {
            BasketItem basketItem = basketItemRepository.findByUserBasketAndItem(userBasket.get(), item)
                    .orElseThrow(() -> new EntityNotFoundException("Basket item not found"));
            basketItem.changeQuantity(quantity);
        }
        else {throw new EntityNotFoundException("Basket not found");}
    }

    @Transactional
    public void removeItem(UUID userUuid, Long itemCode) {
        // 삭제
        UserBasket userBasket = userBasketRepository.findByUserInfo_Uuid(userUuid).orElseThrow(() -> new EntityNotFoundException("Basket not found"));
        Item item = itemRepository.findById(itemCode).orElseThrow(() -> new EntityNotFoundException("Item not found"));
        BasketItem basketItem = basketItemRepository.findByUserBasketAndItem(userBasket, item)
                .orElseThrow(() -> new EntityNotFoundException("Basket item not found"));
        basketItemRepository.delete(basketItem);
    }
}