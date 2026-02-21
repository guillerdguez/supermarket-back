package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.dto.cashregister.CashRegisterFilterRequest;
import com.supermarket.supermarket.dto.inventory.InventoryStatusResponse;
import com.supermarket.supermarket.dto.report.CashRegisterReportResponse;
import com.supermarket.supermarket.dto.report.ProductPerformanceDTO;
import com.supermarket.supermarket.dto.report.ReportFilterRequest;
import com.supermarket.supermarket.dto.report.SalesByBranchDTO;
import com.supermarket.supermarket.dto.report.SalesByCashierDTO;
import com.supermarket.supermarket.dto.report.SalesByProductDTO;
import com.supermarket.supermarket.dto.report.SalesComparisonResponse;
import com.supermarket.supermarket.dto.report.SalesSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReportService {
    SalesSummaryResponse getSalesSummary(ReportFilterRequest filter);

    List<SalesByBranchDTO> getSalesByBranch(ReportFilterRequest filter);

    Page<SalesByProductDTO> getSalesByProduct(ReportFilterRequest filter, Pageable pageable);

    List<SalesByCashierDTO> getSalesByCashier(ReportFilterRequest filter);

    SalesComparisonResponse getSalesComparison(ReportFilterRequest filter);

    InventoryStatusResponse getInventoryStatus(ReportFilterRequest filter);

    Page<ProductPerformanceDTO> getProductPerformance(ReportFilterRequest filter, Pageable pageable);

    CashRegisterReportResponse getCashRegisterReport(CashRegisterFilterRequest filter, Pageable pageable);
}