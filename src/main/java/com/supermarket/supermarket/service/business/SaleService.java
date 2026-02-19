package com.supermarket.supermarket.service.business;

import java.util.List;

import com.supermarket.supermarket.dto.sale.CancelSaleRequest;
import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SaleService {
    List<SaleResponse> getAll();
    SaleResponse getById(Long id);
    SaleResponse create(SaleRequest sale);
    SaleResponse cancel(Long id, CancelSaleRequest request);
    void delete(Long id);
    Page<SaleResponse> getSalesByCashier(Long cashierId, Pageable pageable);
    SaleResponse getSaleByIdAndCashier(Long saleId, Long cashierId);
}