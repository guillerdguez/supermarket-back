package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.dto.payment.PaymentRequest;
import com.supermarket.supermarket.dto.payment.PaymentResponse;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.fixtures.sale.SaleFixtures;
import com.supermarket.supermarket.mapper.PaymentMapper;
import com.supermarket.supermarket.model.Payment;
import com.supermarket.supermarket.model.PaymentType;
import com.supermarket.supermarket.model.Sale;
import com.supermarket.supermarket.model.SaleStatus;
import com.supermarket.supermarket.repository.PaymentRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.service.business.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private SaleRepository saleRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @InjectMocks
    private PaymentServiceImpl paymentService;
 
    @Test
    void registerPayment_shouldSavePayment() {
        Sale sale = SaleFixtures.saleWithDetails();
        sale.setStatus(SaleStatus.REGISTERED);
        sale.setTotal(new BigDecimal("100.00"));

        PaymentRequest request = new PaymentRequest(sale.getId(), new BigDecimal("50.00"), PaymentType.CASH, null);
        Payment payment = Payment.builder().id(10L).build();
        PaymentResponse response = PaymentResponse.builder().id(10L).build();

        given(saleRepository.findById(sale.getId())).willReturn(Optional.of(sale));
        given(paymentRepository.findBySaleId(sale.getId())).willReturn(List.of());
        given(paymentRepository.save(any(Payment.class))).willReturn(payment);
        given(paymentMapper.toResponse(payment)).willReturn(response);

        PaymentResponse result = paymentService.registerPayment(request);

        assertThat(result.getId()).isEqualTo(10L);
        then(paymentRepository).should().save(any(Payment.class));
    }

    @Test
    void registerPayment_whenSaleNotFound_shouldThrowException() {
        given(saleRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.registerPayment(new PaymentRequest(99L, BigDecimal.TEN, PaymentType.CASH, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void registerPayment_whenSaleCancelled_shouldThrowException() {
        Sale sale = SaleFixtures.saleWithDetails();
        sale.setStatus(SaleStatus.CANCELLED);
        given(saleRepository.findById(sale.getId())).willReturn(Optional.of(sale));

        assertThatThrownBy(() -> paymentService.registerPayment(new PaymentRequest(sale.getId(), BigDecimal.TEN, PaymentType.CASH, null)))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void registerPayment_whenTotalExceeded_shouldThrowException() {
        Sale sale = SaleFixtures.saleWithDetails();
        sale.setStatus(SaleStatus.REGISTERED);
        sale.setTotal(new BigDecimal("50.00"));
        given(saleRepository.findById(sale.getId())).willReturn(Optional.of(sale));
        given(paymentRepository.findBySaleId(sale.getId())).willReturn(List.of(Payment.builder().amount(new BigDecimal("30.00")).build()));

        assertThatThrownBy(() -> paymentService.registerPayment(new PaymentRequest(sale.getId(), new BigDecimal("30.00"), PaymentType.CASH, null)))
                .isInstanceOf(InvalidOperationException.class);
    }
}