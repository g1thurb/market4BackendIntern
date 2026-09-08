package com.seyoon.portfolio.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserPaymentService {

    public boolean payByCard(BigDecimal amount, Long paymentId) {
        return true;
    }
    public boolean payByBankBook(BigDecimal amount) {
        return false;
    }
    public boolean payByMobileCarrier(BigDecimal amount) {
        return false;
    }
}
