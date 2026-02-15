package com.supermarket.supermarket.service.business;

import java.util.List;

import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.SaleDetail;

public interface StockService {
    List<Product> validateAndReduceStockBatch(List<SaleDetailRequest> details);

    void restoreStockBatch(List<SaleDetail> existingDetails);
}