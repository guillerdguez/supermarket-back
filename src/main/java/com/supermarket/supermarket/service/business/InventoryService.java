package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.dto.inventory.LowStockAlertResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.model.SaleDetail;

import java.util.List;

public interface InventoryService {

    Integer getStockInBranch(Long branchId, Long productId);

    List<LowStockAlertResponse> getLowStockInBranch(Long branchId);

    List<LowStockAlertResponse> getLowStockGlobal();

    void validateAndReduceStock(Long branchId, Long productId, Integer quantity);

    void restoreStock(Long branchId, Long productId, Integer quantity);

    void validateAndReduceStockBatch(Long branchId, List<SaleDetailRequest> details);

    void restoreStockBatch(Long branchId, List<SaleDetail> details);

    void increaseStock(Long branchId, Long productId, Integer quantity);
}