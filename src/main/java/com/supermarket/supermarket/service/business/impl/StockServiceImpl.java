package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.model.SaleDetail;
import com.supermarket.supermarket.service.business.InventoryService;
import com.supermarket.supermarket.service.business.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements  StockService {

    private final InventoryService inventoryService;

    @Transactional
    @Override
    public void validateAndReduceStockBatch(List<SaleDetailRequest> details, Long branchId) {
        for (SaleDetailRequest detail : details) {
            inventoryService.validateAndReduceStock(branchId, detail.getProductId(), detail.getStock());
        }
    }

    @Transactional
    @Override
    public void restoreStockBatch(List<SaleDetail> details, Long branchId) {
        for (SaleDetail detail : details) {
            inventoryService.restoreStock(branchId, detail.getProduct().getId(), detail.getStock());
        }
    }
}