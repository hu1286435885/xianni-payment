package com.collega.payment_gateway_cip.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.collega.payment_gateway_cip.entity.Transaction;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Transaction> transactionKafkaTemplate;

    public void publishTransactionSuccess(Transaction tx) {
        try {
            transactionKafkaTemplate.send("transaction.success", tx.getOrderId(), tx);
            System.out.println("Publishing to Kafka: " + tx.getId());
        } catch (Exception ex) {
            System.err.println("Failed to publish kafka event: " + ex.getMessage());
        }
    }
}
