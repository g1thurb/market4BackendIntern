package com.seyoon.portfolio.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class SellerItemController {

    @GetMapping("/seller/items")
    public String itemListPage() {
        return "seller/items";
    }

    @GetMapping("/seller/items/new")
    public String newItemPage() {
        return "seller/items/new";
    }

    @GetMapping("/seller/items/{itemCode}")
    public String itemDetailPage(@PathVariable Long itemCode, Model model) {
        model.addAttribute("itemCode", itemCode);
        return "seller/items/detail";
    }
}