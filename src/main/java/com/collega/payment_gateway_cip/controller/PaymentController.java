package com.collega.payment_gateway_cip.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collega.payment_gateway_cip.dto.PaymentRequest;
import com.collega.payment_gateway_cip.dto.PaymentResponse;
import com.collega.payment_gateway_cip.entity.Transaction;
import com.collega.payment_gateway_cip.repository.TransactionRepository;
import com.collega.payment_gateway_cip.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final TransactionRepository transactionRepository;

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        if ("SUCCESS".equalsIgnoreCase(response.status())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable UUID id) {
        Optional<Transaction> t = transactionRepository.findById(id);
        if (t.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(t.get());
    }
}
