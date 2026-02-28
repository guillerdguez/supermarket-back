package com.supermarket.supermarket.mapper;

import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.model.sale.Sale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SaleMapper {
    private final SaleDetailMapper saleDetailMapper;

    public SaleResponse toResponse(Sale sale) {
        if (sale == null) return null;

        return SaleResponse.builder()
                .id(sale.getId())
                .date(sale.getDate())
                .status(sale.getStatus())
                .total(sale.getTotal())
                .branchId(sale.getBranch() != null ? sale.getBranch().getId() : null)
                .branchName(sale.getBranch() != null ? sale.getBranch().getName() : null)
                .createdById(sale.getCreatedBy() != null ? sale.getCreatedBy().getId() : null)
                .createdByUsername(sale.getCreatedBy() != null ? sale.getCreatedBy().getUsername() : null)
                .createdByEmail(sale.getCreatedBy() != null ? sale.getCreatedBy().getEmail() : null)
                .createdAt(sale.getCreatedAt())
                .cancelledById(sale.getCancelledBy() != null ? sale.getCancelledBy().getId() : null)
                .cancelledByUsername(sale.getCancelledBy() != null ? sale.getCancelledBy().getUsername() : null)
                .cancellationReason(sale.getCancellationReason())
                .cancelledAt(sale.getCancelledAt())
                .details(saleDetailMapper.toResponseList(sale.getDetails()))
                .cashRegisterId(sale.getCashRegister() != null ? sale.getCashRegister().getId() : null)
                .cashRegisterStatus(sale.getCashRegister() != null ? sale.getCashRegister().getStatus() : null)
                .build();
    }

    public Sale toEntity(SaleRequest request) {
        if (request == null) return null;

        return Sale.builder()
                .date(request.getDate())
                .details(request.getDetails() != null ? request.getDetails().stream()
                        .map(saleDetailMapper::toEntity)
                        .collect(Collectors.toList())
                        : null)
                .build();
    }

    public List<SaleResponse> toResponseList(List<Sale> sales) {
        if (sales == null) return null;
        return sales.stream()
                .map(this::toResponse)
                .toList();
    }
}