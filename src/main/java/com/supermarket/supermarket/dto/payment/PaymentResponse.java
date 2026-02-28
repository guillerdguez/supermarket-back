package com.supermarket.supermarket.dto.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.supermarket.supermarket.model.sale.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long saleId;
    private BigDecimal amount;
    private PaymentType paymentType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentDate;
    private String reference;
}