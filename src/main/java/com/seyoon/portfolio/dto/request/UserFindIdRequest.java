package com.seyoon.portfolio.dto.request;

import java.time.LocalDate;

public record UserFindIdRequest(
        String username,
        LocalDate birthday,
        String phone
) {
}
