package com.supermarket.supermarket.mapper;

import org.springframework.stereotype.Component;

import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailResponse;
import com.supermarket.supermarket.model.SaleDetail;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SaleDetailMapper {

    public SaleDetailResponse toResponse(SaleDetail entity) {
        if (entity == null)
            return null;

        BigDecimal subtotal = BigDecimal.ZERO;
        if (entity.getPrice() != null && entity.getQuantity() != null) {
            subtotal = entity.getPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));
        }

        return SaleDetailResponse.builder()
                .id(entity.getId())
                .quantity(entity.getQuantity())
                .productName(entity.getProduct() != null ? entity.getProduct().getName() : null)
                .unitPrice(entity.getPrice())
                .subtotal(subtotal)
                .build();
    }

    public SaleDetail toEntity(SaleDetailRequest request) {
        if (request == null)
            return null;

        return SaleDetail.builder()
                .quantity(request.getQuantity())
                .build();
    }

    public List<SaleDetailResponse> toResponseList(List<SaleDetail> entities) {
        if (entities == null)
            return null;

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}