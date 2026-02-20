package com.supermarket.supermarket.dto.cashregister;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.supermarket.supermarket.model.PaymentType;
import lombok.*;
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