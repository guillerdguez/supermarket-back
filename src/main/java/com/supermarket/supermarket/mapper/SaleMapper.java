package com.supermarket.supermarket.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.model.Sale;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class SaleMapper {

        private final SaleDetailMapper saleDetailMapper;

        public SaleResponse toResponse(Sale sale) {
                if (sale == null)
                        return null;

                return SaleResponse.builder()
                                .id(sale.getId())
                                .date(sale.getDate())
                                .status(sale.getStatus())
                                .total(sale.getTotal())
                                .branchId(sale.getBranch() != null ? sale.getBranch().getId() : null)
                                .branchName(sale.getBranch() != null ? sale.getBranch().getName() : null)
                                .details(saleDetailMapper.toResponseList(sale.getDetails()))
                                .build();
        }

        public Sale toEntity(SaleRequest request) {
                if (request == null)
                        return null;

                return Sale.builder()
                                .date(request.getDate())
                                .details(request.getDetails() != null ? request.getDetails().stream()
                                                .map(saleDetailMapper::toEntity)
                                                .collect(Collectors.toList())
                                                : null)
                                .build();
        }

        public List<SaleResponse> toResponseList(List<Sale> sales) {
                if (sales == null)
                        return null;

                return sales.stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
        }
}