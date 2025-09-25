package com.collega.payment_gateway_cip.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.collega.payment_gateway_cip.dto.PaymentRequest;
import com.collega.payment_gateway_cip.dto.PaymentResponse;
import com.collega.payment_gateway_cip.entity.Transaction;
import com.collega.payment_gateway_cip.enums.Channel;
import com.collega.payment_gateway_cip.enums.TransactionStatus;
import com.collega.payment_gateway_cip.event.KafkaProducerService;
import com.collega.payment_gateway_cip.feign.BillerClient;
import com.collega.payment_gateway_cip.feign.CoreBankClient;
import com.collega.payment_gateway_cip.feign.dto.BillerRequest;
import com.collega.payment_gateway_cip.feign.dto.BillerResponse;
import com.collega.payment_gateway_cip.feign.dto.CoreBankDebitRequest;
import com.collega.payment_gateway_cip.feign.dto.CoreBankDebitResponse;
import com.collega.payment_gateway_cip.repository.TransactionRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final CoreBankClient coreBankClient;
    private final BillerClient billerClient;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // Basic Validation
        if (request.orderId() == null || request.orderId().isBlank()) {
            return new PaymentResponse(null, null, "FAILED", null, null, "Order ID is required");
        }
        if (request.amount() == null || request.amount().doubleValue() <= 0) {
            return new PaymentResponse(null, request.orderId(), "FAILED", null, null, "Amount must be greater than zero");
        }

        // Check existing order
        Optional<Transaction> existing = transactionRepository.findByOrderId(request.orderId());
        if (existing.isPresent()) {
            Transaction t = existing.get();
            return new PaymentResponse(t.getId().toString(), t.getOrderId(), t.getStatus().name(),
                    t.getCorebankReference(), t.getBillerReference(), "Order ID already processed");
        }

        // Create pending transaction
        Transaction tx = new Transaction();
        tx.setOrderId(request.orderId());
        tx.setAmount(request.amount());
        tx.setAccount(request.account());
        tx.setCurrency(request.currency() == null ? "IDR" : request.currency());
        tx.setPaymentMethod(request.paymentMethod());
        try {
            tx.setChannel(Channel.valueOf(request.channel().toUpperCase()));
        } catch (Exception ex) {
            tx.setChannel(Channel.MOBILE_BANKING);
        }
        tx.setStatus(TransactionStatus.PENDING);
        tx.setCreatedAt(OffsetDateTime.now());
        tx.setUpdatedAt(OffsetDateTime.now());
        transactionRepository.save(tx);

        // --- CoreBank Debit with fallback ---
        CoreBankDebitRequest debitRequest = new CoreBankDebitRequest(tx.getAccount(), tx.getCurrency(), tx.getAmount(), tx.getOrderId());
        CoreBankDebitResponse debitResponse;
        try {
            debitResponse = callCoreBankWithFallback(debitRequest);
        } catch (Exception ex) {
            debitResponse = coreBankFallback(debitRequest, ex);
        }

        tx.setCorebankReference(debitResponse.getReference());
        if (!debitResponse.isSuccess()) {
            tx.setStatus(TransactionStatus.FAILED);
            tx.setUpdatedAt(OffsetDateTime.now());
            transactionRepository.save(tx);
            return new PaymentResponse(tx.getId().toString(), tx.getOrderId(),
                    "FAILED", tx.getCorebankReference(), null, debitResponse.getMessage());
        }

        tx.setUpdatedAt(OffsetDateTime.now());
        transactionRepository.save(tx);

        // --- Biller call with fallback ---
        BillerRequest billerRequest = new BillerRequest(tx.getOrderId(), tx.getAccount(), tx.getAmount(), tx.getPaymentMethod());
        BillerResponse billerResponse;
        try {
            billerResponse = callBillerWithFallback(billerRequest);
        } catch (Exception ex) {
            billerResponse = billerFallback(billerRequest, ex);
        }

        tx.setBillerReference(billerResponse.getReference());
        if (!billerResponse.isSuccess()) {
            tx.setStatus(TransactionStatus.FAILED);
            tx.setUpdatedAt(OffsetDateTime.now());
            transactionRepository.save(tx);
            return new PaymentResponse(tx.getId().toString(), tx.getOrderId(),
                    "FAILED", tx.getCorebankReference(), tx.getBillerReference(), billerResponse.getMessage());
        }

        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setUpdatedAt(OffsetDateTime.now());
        transactionRepository.save(tx);

        kafkaProducerService.publishTransactionSuccess(tx);

        return new PaymentResponse(tx.getId().toString(), tx.getOrderId(),
                tx.getStatus().name(), tx.getCorebankReference(), tx.getBillerReference(), "Payment successful");
    }

    @CircuitBreaker(name = "corebank-cb", fallbackMethod = "coreBankFallback")
    public CoreBankDebitResponse callCoreBankWithFallback(CoreBankDebitRequest request) {
        return coreBankClient.debit(request);
    }

    public CoreBankDebitResponse coreBankFallback(CoreBankDebitRequest request, Throwable t) {
        return new CoreBankDebitResponse(
                "CB-MOCK-" + Instant.now().toEpochMilli(),
                true,
                "Fallback executed: CoreBank service unavailable."
        );
    }

    @CircuitBreaker(name = "biller-cb", fallbackMethod = "billerFallback")
    public BillerResponse callBillerWithFallback(BillerRequest request) {
        return billerClient.pay(request);
    }

    public BillerResponse billerFallback(BillerRequest request, Throwable t) {
        return new BillerResponse(
                true,
                "BILLER-MOCK-" + Instant.now().toEpochMilli(),
                "Fallback executed: Biller service unavailable."
        );
    }
}
