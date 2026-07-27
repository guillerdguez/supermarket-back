package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.dto.inventory.BranchInventoryResponse;
import com.supermarket.supermarket.dto.inventory.LowStockAlertResponse;
import com.supermarket.supermarket.dto.inventory.StockAdjustmentRequest;
import com.supermarket.supermarket.dto.inventory.StockUpdateRequest;
import com.supermarket.supermarket.dto.inventory.TotalStockResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.model.branch.Branch;
import com.supermarket.supermarket.model.product.Product;
import com.supermarket.supermarket.model.sale.SaleDetail;

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

    List<BranchInventoryResponse> getBranchInventory(Long branchId);

    BranchInventoryResponse updateStock(Long branchId, Long productId, StockUpdateRequest request);

    BranchInventoryResponse adjustStock(Long branchId, Long productId, StockAdjustmentRequest request);

    TotalStockResponse getTotalStockByProduct(Long productId);

    Integer getMinStockInBranch(Long branchId, Long productId);

    void initializeInventoryForNewProduct(Product product);

    void initializeInventoryForNewBranch(Branch branch);


}