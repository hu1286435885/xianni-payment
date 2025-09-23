package com.collega.payment_gateway_cip.dto;

import java.math.BigDecimal;

public record PaymentRequest(
        String orderId,
        String channel,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String account
        ) {

}
