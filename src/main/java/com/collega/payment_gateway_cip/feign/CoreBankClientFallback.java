package com.collega.payment_gateway_cip.feign;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.collega.payment_gateway_cip.feign.dto.CoreBankDebitRequest;
import com.collega.payment_gateway_cip.feign.dto.CoreBankDebitResponse;

@Component
public class CoreBankClientFallback implements CoreBankClient {

    @Override
    public CoreBankDebitResponse debit(CoreBankDebitRequest request) {
        boolean success = true; // bisa ubah false untuk simulasi gagal
        String reference = "CB-MOCK-" + Instant.now().toEpochMilli();
        String message = "Fallback debit executed. CoreBank service unavailable.";

        return new CoreBankDebitResponse(reference, success, message);
    }

}
