package com.supermarket.supermarket.controller;

 
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
import com.supermarket.supermarket.service.business.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints for business reports and analytics")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/sales/summary")
    @Operation(summary = "Get overall sales summary with optional filters")
    public ResponseEntity<SalesSummaryResponse> getSalesSummary(
            @Valid @ModelAttribute ReportFilterRequest filter) {
        return ResponseEntity.ok(reportService.getSalesSummary(filter));
    }

    @GetMapping("/sales/by-branch")
    @Operation(summary = "Get sales grouped by branch")
    public ResponseEntity<List<SalesByBranchDTO>> getSalesByBranch(
            @Valid @ModelAttribute ReportFilterRequest filter) {
        return ResponseEntity.ok(reportService.getSalesByBranch(filter));
    }

    @GetMapping("/sales/by-product")
    @Operation(summary = "Get sales grouped by product with pagination")
    public ResponseEntity<Page<SalesByProductDTO>> getSalesByProduct(
            @Valid @ModelAttribute ReportFilterRequest filter,
            @PageableDefault(page = 0, size = 20, sort = "totalRevenue", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reportService.getSalesByProduct(filter, pageable));
    }

    @GetMapping("/sales/by-cashier")
    @Operation(summary = "Get sales grouped by cashier")
    public ResponseEntity<List<SalesByCashierDTO>> getSalesByCashier(
            @Valid @ModelAttribute ReportFilterRequest filter) {
        return ResponseEntity.ok(reportService.getSalesByCashier(filter));
    }

    @GetMapping("/sales/comparison")
    @Operation(summary = "Compare current period sales against the equivalent previous period")
    public ResponseEntity<SalesComparisonResponse> getSalesComparison(
            @Valid @ModelAttribute ReportFilterRequest filter) {
        return ResponseEntity.ok(reportService.getSalesComparison(filter));
    }

    @GetMapping("/inventory/status")
    @Operation(summary = "Get overall inventory status")
    public ResponseEntity<InventoryStatusResponse> getInventoryStatus(
            @Valid @ModelAttribute ReportFilterRequest filter) {
        return ResponseEntity.ok(reportService.getInventoryStatus(filter));
    }

    @GetMapping("/inventory/performance")
    @Operation(summary = "Get product performance with turnover rate and pagination")
    public ResponseEntity<Page<ProductPerformanceDTO>> getProductPerformance(
            @Valid @ModelAttribute ReportFilterRequest filter,
            @PageableDefault(page = 0, size = 20, sort = "totalSold", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reportService.getProductPerformance(filter, pageable));
    }

    @GetMapping("/cash-registers")
    @Operation(summary = "Get cash register closure report with discrepancy detection")
    public ResponseEntity<CashRegisterReportResponse> getCashRegisterReport(
            @Valid @ModelAttribute CashRegisterFilterRequest filter,
            @PageableDefault(page = 0, size = 20, sort = "closingTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reportService.getCashRegisterReport(filter, pageable));
    }
}