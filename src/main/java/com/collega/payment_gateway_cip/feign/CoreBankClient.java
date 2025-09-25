package com.collega.payment_gateway_cip.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import com.collega.payment_gateway_cip.feign.dto.CoreBankDebitRequest;
import com.collega.payment_gateway_cip.feign.dto.CoreBankDebitResponse;

@FeignClient(name = "corebank", url = "${integration.corebank.url}")
public interface CoreBankClient {

    @PostMapping("/api/corebank/debit")
    CoreBankDebitResponse debit(CoreBankDebitRequest request);

}
