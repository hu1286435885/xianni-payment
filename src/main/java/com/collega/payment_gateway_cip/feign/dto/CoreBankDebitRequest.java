package com.collega.payment_gateway_cip.feign.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoreBankDebitRequest {

    private String account;
    private String currency;
    private BigDecimal amount;
    private String reference;

}
