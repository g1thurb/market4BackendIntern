package com.seyoon.portfolio.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SellerOrderController {

    @GetMapping("/seller/orders")
    public String orderListPage() {
        return "seller/orders";
    }

    @GetMapping("/seller/orders/{orderId}")
    public String orderDetailPage(@PathVariable Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "seller/orders/detail";
    }
}