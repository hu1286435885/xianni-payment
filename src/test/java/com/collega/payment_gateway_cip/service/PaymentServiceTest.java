package com.collega.payment_gateway_cip.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.collega.payment_gateway_cip.dto.PaymentRequest;
import com.collega.payment_gateway_cip.dto.PaymentResponse;
import com.collega.payment_gateway_cip.entity.Transaction;
import com.collega.payment_gateway_cip.enums.TransactionStatus;
import com.collega.payment_gateway_cip.event.KafkaProducerService;
import com.collega.payment_gateway_cip.feign.BillerClient;
import com.collega.payment_gateway_cip.feign.CoreBankClient;
import com.collega.payment_gateway_cip.feign.dto.BillerRequest;
import com.collega.payment_gateway_cip.feign.dto.BillerResponse;
import com.collega.payment_gateway_cip.feign.dto.CoreBankDebitRequest;
import com.collega.payment_gateway_cip.feign.dto.CoreBankDebitResponse;
import com.collega.payment_gateway_cip.repository.TransactionRepository;

class PaymentServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CoreBankClient coreBankClient;

    @Mock
    private BillerClient billerClient;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            if (tx.getId() == null) {
                tx.setId(UUID.randomUUID());
            }
            return tx;
        });
    }

    @Test
    void createPayment_shouldFail_whenOrderIdIsMissing() {
        PaymentRequest request = new PaymentRequest("", "MOBILE_BANKING",
                BigDecimal.valueOf(100), "IDR", "VA", "12345");

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("FAILED", response.status());
        assertEquals("Order ID is required", response.message());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createPayment_shouldFail_whenAmountIsInvalid() {
        PaymentRequest request = new PaymentRequest("ORD123", "MOBILE_BANKING",
                BigDecimal.ZERO, "IDR", "VA", "12345");

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("FAILED", response.status());
        assertEquals("Amount must be greater than zero", response.message());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createPayment_shouldReturnExistingTransaction_whenOrderAlreadyExists() {
        Transaction existing = new Transaction();
        existing.setId(UUID.randomUUID());
        existing.setOrderId("ORD123");
        existing.setStatus(TransactionStatus.SUCCESS);
        existing.setCorebankReference("CB123");
        existing.setBillerReference("BILLER123");

        when(transactionRepository.findByOrderId("ORD123")).thenReturn(Optional.of(existing));

        PaymentRequest request = new PaymentRequest("ORD123", "MOBILE_BANKING",
                BigDecimal.valueOf(100), "IDR", "VA", "12345");

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("Order ID already processed", response.message());
        assertEquals("SUCCESS", response.status());
    }

    @Test
    void createPayment_shouldFail_whenCoreBankFails() {
        when(coreBankClient.debit(any(CoreBankDebitRequest.class)))
                .thenReturn(new CoreBankDebitResponse("REF123", false, "Insufficient funds"));

        PaymentRequest request = new PaymentRequest("ORD456", "MOBILE_BANKING",
                BigDecimal.valueOf(200), "IDR", "VA", "12345");

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("FAILED", response.status());
        assertEquals("Insufficient funds", response.message());
        verify(transactionRepository, atLeastOnce()).save(any());
    }

    @Test
    void createPayment_shouldFail_whenBillerFails() {
        when(coreBankClient.debit(any(CoreBankDebitRequest.class)))
                .thenReturn(new CoreBankDebitResponse("REF123", true, "OK"));

        when(billerClient.pay(any(BillerRequest.class)))
                .thenReturn(new BillerResponse(false, "BILL123", "Biller failed"));

        PaymentRequest request = new PaymentRequest("ORD789", "MOBILE_BANKING",
                BigDecimal.valueOf(300), "IDR", "VA", "12345");

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("FAILED", response.status());
        assertEquals("Biller failed", response.message());
        verify(transactionRepository, atLeastOnce()).save(any());
    }

    @Test
    void createPayment_shouldSucceed_whenCoreBankAndBillerReturnSuccess() {
        when(coreBankClient.debit(any(CoreBankDebitRequest.class)))
                .thenReturn(new CoreBankDebitResponse("CB123", true, "OK"));

        when(billerClient.pay(any(BillerRequest.class)))
                .thenReturn(new BillerResponse(true, "BILL123", "Payment processed"));

        PaymentRequest request = new PaymentRequest("ORD999", "MOBILE_BANKING",
                BigDecimal.valueOf(500), "IDR", "VA", "12345");

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("SUCCESS", response.status());
        assertEquals("Payment successful", response.message());
        verify(kafkaProducerService, times(1)).publishTransactionSuccess(any(Transaction.class));
    }
}
