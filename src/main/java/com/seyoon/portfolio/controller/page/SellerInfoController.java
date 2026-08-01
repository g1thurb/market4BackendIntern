package com.seyoon.portfolio.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SellerInfoController {

    @GetMapping("/seller/login")
    public String loginPage() {
        return "seller/login";
    }

    @GetMapping("/seller/sign-up")
    public String signUpPage() {
        return "seller/sign-up";
    }

    @GetMapping("/seller/find-id")
    public String findIdPage() {
        return "seller/find-id";
    }

    @GetMapping("/seller/reset-pw")
    public String resetPasswordPage() {
        return "seller/reset-pw";
    }

    @GetMapping("/seller/profile")
    public String profilePage() {
        return "seller/profile";
    }
}