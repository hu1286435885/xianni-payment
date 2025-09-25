package com.collega.payment_gateway_cip.controller;

import com.collega.payment_gateway_cip.dto.PaymentRequest;
import com.collega.payment_gateway_cip.dto.PaymentResponse;
import com.collega.payment_gateway_cip.entity.Transaction;
import com.collega.payment_gateway_cip.repository.TransactionRepository;
import com.collega.payment_gateway_cip.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private TransactionRepository transactionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void create_shouldReturnSuccessResponse() throws Exception {
        PaymentRequest req = new PaymentRequest(
                "order-123", "MOBILE_BANKING", new BigDecimal("1000.00"),
                "IDR", "VA", "1234567890"
        );

        PaymentResponse resp = new PaymentResponse(
                UUID.randomUUID().toString(), "order-123", "SUCCESS",
                "CB123", "BILL123", "Payment successful"
        );

        Mockito.when(paymentService.createPayment(Mockito.any(PaymentRequest.class)))
                .thenReturn(resp);

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void get_shouldReturnTransaction_whenExists() throws Exception {
        UUID id = UUID.randomUUID();
        Transaction t = new Transaction();
        t.setId(id);
        t.setOrderId("order-123");
        t.setAmount(new BigDecimal("1000.00"));
        t.setCurrency("IDR");
        t.setPaymentMethod("VA");

        Mockito.when(transactionRepository.findById(id)).thenReturn(Optional.of(t));

        mockMvc.perform(get("/api/payments/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order-123"))
                .andExpect(jsonPath("$.amount").value(1000.0))
                .andExpect(jsonPath("$.currency").value("IDR"))
                .andExpect(jsonPath("$.paymentMethod").value("VA"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void get_shouldReturnNotFound_whenTransactionNotExists() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/" + id))
                .andExpect(status().isNotFound());
    }
}
