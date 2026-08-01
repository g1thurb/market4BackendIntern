package com.seyoon.portfolio.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserInfoController {

    @GetMapping("/user/login")
    public String loginPage() {
        return "user/login";
    }

    @GetMapping("/user/sign-up")
    public String signUpPage() {
        return "user/sign-up";
    }

    @GetMapping("/user/find-id")
    public String findIdPage() {
        return "user/find-id";
    }

    @GetMapping("/user/reset-pw")
    public String resetPasswordPage() {
        return "user/reset-pw";
    }

    @GetMapping("/user/profile")
    public String profilePage() {
        return "user/profile";
    }
}