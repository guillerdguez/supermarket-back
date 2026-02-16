package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.dto.inventory.LowStockAlertResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.model.SaleDetail;
import java.util.List;

public interface InventoryService {

    Integer getStockInBranch(Long branchId, Long productId);

    List<LowStockAlertResponse> getLowStockInBranch(Long branchId);

    List<LowStockAlertResponse> getLowStockGlobal();


}