package com.supermarket.supermarket.fixtures.payment;

import com.supermarket.supermarket.dto.payment.PaymentRequest;
import com.supermarket.supermarket.dto.payment.PaymentResponse;
import com.supermarket.supermarket.fixtures.sale.SaleFixtures;
import com.supermarket.supermarket.model.sale.Payment;
import com.supermarket.supermarket.model.sale.PaymentType;
import com.supermarket.supermarket.model.sale.Sale;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@UtilityClass
public class PaymentFixtures {

    public static Payment cashPayment() {
        return cashPayment(1L, SaleFixtures.saleWithDetails());
    }

    public static Payment cashPayment(Long id, Sale sale) {
        return Payment.builder()
                .id(id)
                .sale(sale)
                .amount(new BigDecimal("50.00"))
                .paymentType(PaymentType.CASH)
                .paymentDate(LocalDateTime.now())
                .build();
    }

    public static Payment cardPayment() {
        return cardPayment(2L, SaleFixtures.saleWithDetails());
    }

    public static Payment cardPayment(Long id, Sale sale) {
        return Payment.builder()
                .id(id)
                .sale(sale)
                .amount(new BigDecimal("75.50"))
                .paymentType(PaymentType.CARD)
                .paymentDate(LocalDateTime.now())
                .reference("REF123")
                .build();
    }

    public static PaymentRequest validCashPaymentRequest() {
        return PaymentRequest.builder()
                .saleId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentType(PaymentType.CASH)
                .build();
    }

    public static PaymentRequest validCardPaymentRequest() {
        return PaymentRequest.builder()
                .saleId(1L)
                .amount(new BigDecimal("75.50"))
                .paymentType(PaymentType.CARD)
                .reference("REF123")
                .build();
    }

    public static PaymentResponse paymentResponse() {
        return PaymentResponse.builder()
                .id(1L)
                .saleId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentType(PaymentType.CASH)
                .paymentDate(LocalDateTime.now())
                .build();
    }
}