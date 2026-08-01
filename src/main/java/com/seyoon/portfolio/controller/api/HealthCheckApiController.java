package com.seyoon.portfolio.controller.api;

import com.seyoon.portfolio.repository.UserInfoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckApiController {

    private final UserInfoRepository userInfoRepository;

    public HealthCheckApiController(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/api/health/db")
    public String dbHealth() {
        long userCount = userInfoRepository.count();
        return "DB connected. user count = " + userCount;
    }
}