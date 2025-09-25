package com.collega.payment_gateway_cip.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.collega.payment_gateway_cip.entity.Transaction;

class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Transaction> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setOrderId("ORD-123");
        transaction.setAmount(BigDecimal.valueOf(1000));
        transaction.setCurrency("IDR");
        transaction.setAccount("12345");
        transaction.setPaymentMethod("VA");
        transaction.setCreatedAt(OffsetDateTime.now());
        transaction.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void publishTransactionSuccess_shouldSendMessageToKafka() {
        // Mock KafkaTemplate agar tidak benar-benar mengirim
        when(kafkaTemplate.send(eq("transaction.success"), eq("ORD-123"), any(Transaction.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        new SendResult<>(null, new RecordMetadata(null, 0, 0, 0L, 0L, 0, 0))
                ));

        kafkaProducerService.publishTransactionSuccess(transaction);

        verify(kafkaTemplate, times(1))
                .send("transaction.success", "ORD-123", transaction);
    }

    @Test
    void publishTransactionSuccess_shouldHandleException() {
        when(kafkaTemplate.send(any(), any(), any()))
                .thenThrow(new RuntimeException("Kafka down"));

        // Tidak ada exception yang dilempar keluar, hanya log error
        kafkaProducerService.publishTransactionSuccess(transaction);

        verify(kafkaTemplate, times(1))
                .send("transaction.success", "ORD-123", transaction);
    }
}
