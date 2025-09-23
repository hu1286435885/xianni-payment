package com.collega.payment_gateway_cip.dto;

public record PaymentResponse(
        String transactionId,
        String orderId,
        String status,
        String coreBankReference,
        String billerReference,
        String message
        ) {

}
