package com.collega.payment_gateway_cip.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoreBankDebitResponse {

    private String reference;
    private boolean success;
    private String message;

}
