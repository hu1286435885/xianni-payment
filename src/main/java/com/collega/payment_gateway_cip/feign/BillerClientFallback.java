package com.collega.payment_gateway_cip.feign;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.collega.payment_gateway_cip.feign.dto.BillerRequest;
import com.collega.payment_gateway_cip.feign.dto.BillerResponse;

@Component
public class BillerClientFallback implements BillerClient {

    @Override
    public BillerResponse pay(BillerRequest request) {
        boolean success = true; // bisa diubah false untuk simulasi gagal
        String reference = "BILLER-MOCK-" + Instant.now().toEpochMilli();
        String message = "Fallback payment executed. Biller service unavailable.";

        return new BillerResponse(success, reference, message);
    }

}
