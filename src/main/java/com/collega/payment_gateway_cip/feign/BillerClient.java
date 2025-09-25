package com.collega.payment_gateway_cip.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import com.collega.payment_gateway_cip.feign.dto.BillerRequest;
import com.collega.payment_gateway_cip.feign.dto.BillerResponse;

@FeignClient(name = "biller", url = "${integration.biller.url}")
public interface BillerClient {

    @PostMapping("/api/biller/pay")
    BillerResponse pay(BillerRequest request);

}
