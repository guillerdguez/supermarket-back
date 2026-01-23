package com.supermarket.supermarket.dto.saleDetail;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleDetailResponse {
    private Long id;
    private Integer quantity;
    private String productName;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}