package com.seyoon.portfolio.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UserOrderController {

    @GetMapping("/user/basket")
    public String basketPage() {
        return "user/basket";
    }

    @GetMapping("/user/orders/new")
    public String newOrderPage() {
        return "user/orders/new";
    }

    @GetMapping("/user/orders")
    public String orderListPage() {
        return "user/orders";
    }

    @GetMapping("/user/orders/{orderId}")
    public String orderDetailPage(@PathVariable Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "user/orders/detail";
    }
}