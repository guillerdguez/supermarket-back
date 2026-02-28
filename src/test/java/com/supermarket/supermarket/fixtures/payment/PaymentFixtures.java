package com.supermarket.supermarket.fixtures.payment;

import com.supermarket.supermarket.fixtures.sale.SaleFixtures;
import com.supermarket.supermarket.model.sale.Payment;
import com.supermarket.supermarket.model.sale.PaymentType;
import com.supermarket.supermarket.model.sale.Sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}