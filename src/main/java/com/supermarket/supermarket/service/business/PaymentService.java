package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.dto.payment.PaymentRequest;
import com.supermarket.supermarket.dto.payment.PaymentResponse;
import java.util.List;

public interface PaymentService {
    PaymentResponse registerPayment(PaymentRequest request);
    List<PaymentResponse> getPaymentsBySale(Long saleId);
}