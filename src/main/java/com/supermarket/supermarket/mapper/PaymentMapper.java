package com.supermarket.supermarket.mapper;

import com.supermarket.supermarket.dto.payment.PaymentResponse;
import com.supermarket.supermarket.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) return null;
        return PaymentResponse.builder()
                .id(payment.getId())
                .saleId(payment.getSale() != null ? payment.getSale().getId() : null)
                .amount(payment.getAmount())
                .paymentType(payment.getPaymentType())
                .paymentDate(payment.getPaymentDate())
                .reference(payment.getReference())
                .build();
    }
}