package com.seyoon.portfolio.dto.request;

import java.time.LocalDate;

public record UserResetPasswordRequest(
        String id,
        String username,
        LocalDate birthday,
        String phone
) {
}
