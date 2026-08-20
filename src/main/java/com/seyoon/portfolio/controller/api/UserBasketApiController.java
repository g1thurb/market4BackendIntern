package com.seyoon.portfolio.controller.api;

import com.seyoon.portfolio.dto.request.BasketItemAddRequest;
import com.seyoon.portfolio.dto.request.BasketQuantityChangeRequest;
import com.seyoon.portfolio.dto.response.BasketGetResponse;
import com.seyoon.portfolio.service.UserBasketService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user/basket")
public class UserBasketApiController {

    private final UserBasketService userBasketService;

    public UserBasketApiController(UserBasketService userBasketService) {
        this.userBasketService = userBasketService;
    }

    //@RequestParam UUID userUuid은 보언인증파트 끝나면 @RequestParam을 없애기 + 추가할 것 있으면 하기
    @GetMapping
    public BasketGetResponse getUserBasket(@RequestParam UUID userUuid) {// 일단 테스트를 위해
        return userBasketService.getBasket(userUuid);
    }
//    @GetMapping("")
//    public BasketGetResponse getUserBasket(UUID userUuid) {//session같은 보안인증 security로 만들면 없애기(보안인증할거임)
//        return userBasketService.getBasket(userUuid);
//    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/items/{itemCode}")
    public void patchItems(@RequestParam UUID userUuid, @PathVariable("itemCode") Long itemCode, @RequestBody BasketQuantityChangeRequest request) {
        userBasketService.changeQuantity(userUuid, itemCode, request.quantity());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/items/{itemCode}")
    public void addItems(@RequestParam UUID userUuid, @PathVariable("itemCode") Long itemCode, @RequestBody BasketItemAddRequest request) {
        userBasketService.addItem(userUuid, itemCode, request.quantity());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/items/{itemCode}")
    public void deleteItems(@RequestParam UUID userUuid, @PathVariable("itemCode") Long itemCode) {
        userBasketService.removeItem(userUuid, itemCode);
    }

    //InvalidQuantityException
    //→ 400 BAD_REQUEST
    //
    //EntityNotFoundException
    //→ 404 NOT_FOUND
    //
    //ItemOutOfStockException
    //→ 409 CONFLICT
    //
    //InsufficientStockException
    //→ 409 CONFLICT
    //이런건 spring이 알아서 매핑 안해주고 500같은걸로 나가니 직접 매핑해줘야함 DTO response랑 같이 -> @RestControllerAdvice쓰기
}
//추후 필요하면 ResponseEntity 사용해서 응답 헤더부터 바디까지 쓰기