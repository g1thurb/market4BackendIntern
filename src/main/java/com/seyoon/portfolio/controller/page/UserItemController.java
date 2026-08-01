package com.seyoon.portfolio.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UserItemController {

    @GetMapping("/user/items")
    public String itemListPage() {
        return "user/items/list";
    }

    @GetMapping("/user/items/{itemCode}")
    public String itemDetailPage(@PathVariable Long itemCode, Model model) {
        model.addAttribute("itemCode", itemCode);
        return "user/items/detail";
    }

    @GetMapping("/user/search")
    public String searchPage() {
        return "user/items/search";
    }
}