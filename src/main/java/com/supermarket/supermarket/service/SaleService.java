package com.supermarket.supermarket.service;


import java.util.List;

import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;

public interface SaleService
 {
    List<SaleResponse> getAll();

    SaleResponse getById(Long id);

    SaleResponse create(SaleRequest Sale);

    SaleResponse update(Long id, SaleRequest Sale);

    void delete(Long id);
}