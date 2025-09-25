package com.collega.payment_gateway_cip.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.collega.payment_gateway_cip.enums.Channel;
import com.collega.payment_gateway_cip.enums.TransactionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "transactions", indexes = {
    @Index(columnList = "order_id", name = "idx_order_id"),})
public class Transaction {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId;

    // enum: MOBILE_BANKING, INTERNET_BANKING, ATM
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    private String account;

    private String currency = "IDR";

    @Column(name = "payment_method")
    private String paymentMethod;

    // enum: PENDING, SUCCESS, FAILED
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "corebank_reference", nullable = true)
    private String corebankReference;

    @Column(name = "biller_reference", nullable = true)
    private String billerReference;

    // Timestamp auto generated
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    // Timestamp auto generated
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
