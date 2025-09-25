package com.collega.payment_gateway_cip.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillerResponse {

    private boolean success;
    private String reference;
    private String message;

}
