package com.supermarket.supermarket.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.supermarket.controller.PaymentController;
import com.supermarket.supermarket.dto.payment.PaymentRequest;
import com.supermarket.supermarket.exception.GlobalExceptionHandler;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.fixtures.payment.PaymentFixtures;
import com.supermarket.supermarket.service.business.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /payments - should return 201 and Location header")
    void registerPayment_ShouldReturn201() throws Exception {
        given(paymentService.registerPayment(any())).willReturn(PaymentFixtures.paymentResponse());

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PaymentFixtures.validCashPaymentRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paymentType").value("CASH"));
    }

    @Test
    @DisplayName("POST /payments - should return 400 when request is invalid")
    void registerPayment_InvalidRequest_ShouldReturn400() throws Exception {
        PaymentRequest invalid = PaymentRequest.builder()
                .saleId(null)
                .amount(null)
                .paymentType(null)
                .build();

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /payments - should return 404 when sale does not exist")
    void registerPayment_SaleNotFound_ShouldReturn404() throws Exception {
        given(paymentService.registerPayment(any()))
                .willThrow(new ResourceNotFoundException("Sale not found"));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PaymentFixtures.validCashPaymentRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /payments - should return 400 when sale is cancelled")
    void registerPayment_CancelledSale_ShouldReturn400() throws Exception {
        given(paymentService.registerPayment(any()))
                .willThrow(new InvalidOperationException("Cannot register payment for a cancelled sale"));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PaymentFixtures.validCashPaymentRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /payments - should return 400 when total payment exceeds sale total")
    void registerPayment_ExceedsSaleTotal_ShouldReturn400() throws Exception {
        given(paymentService.registerPayment(any()))
                .willThrow(new InvalidOperationException("Total payment exceeds sale total"));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PaymentFixtures.validCardPaymentRequest())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /payments/sale/{saleId} - should return list of payments")
    void getPaymentsBySale_ShouldReturnList() throws Exception {
        given(paymentService.getPaymentsBySale(1L)).willReturn(List.of(PaymentFixtures.paymentResponse()));

        mockMvc.perform(get("/payments/sale/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
