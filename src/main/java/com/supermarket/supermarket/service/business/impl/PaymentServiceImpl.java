package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.payment.PaymentRequest;
import com.supermarket.supermarket.dto.payment.PaymentResponse;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.PaymentMapper;
import com.supermarket.supermarket.model.Payment;
import com.supermarket.supermarket.model.Sale;
import com.supermarket.supermarket.repository.PaymentRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.service.business.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final SaleRepository saleRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponse registerPayment(PaymentRequest request) {
        Sale sale = saleRepository.findById(request.getSaleId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        if (sale.getStatus() == com.supermarket.supermarket.model.SaleStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot register payment for a cancelled sale");
        }

        BigDecimal totalPaid = paymentRepository.findBySaleId(sale.getId()).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalPaid.add(request.getAmount()).compareTo(sale.getTotal()) > 0) {
            throw new InvalidOperationException("Total payment exceeds sale total");
        }

        Payment payment = Payment.builder()
                .sale(sale)
                .amount(request.getAmount())
                .paymentType(request.getPaymentType())
                .paymentDate(LocalDateTime.now())
                .reference(request.getReference())
                .build();

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsBySale(Long saleId) {
        return paymentRepository.findBySaleId(saleId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }
}