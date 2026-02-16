package com.supermarket.supermarket.service.business;

import java.util.List;

import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.model.SaleDetail;
import org.springframework.transaction.annotation.Transactional;

public interface StockService {
    @Transactional
    void validateAndReduceStockBatch(List<SaleDetailRequest> details, Long branchId);

    @Transactional
    void restoreStockBatch(List<SaleDetail> details, Long branchId);
}