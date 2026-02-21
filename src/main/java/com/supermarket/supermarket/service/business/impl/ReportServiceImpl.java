package com.supermarket.supermarket.service.business.impl;

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
import com.supermarket.supermarket.repository.BranchInventoryRepository;
import com.supermarket.supermarket.repository.BranchInventoryRepository.InventoryStatusProjection;
import com.supermarket.supermarket.repository.CashRegisterRepository;
import com.supermarket.supermarket.repository.CashRegisterRepository.ClosureDiscrepancyProjection;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.repository.SaleRepository.PeriodSummaryProjection;
import com.supermarket.supermarket.service.business.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportServiceImpl implements ReportService {
    private final SaleRepository saleRepository;
    private final BranchInventoryRepository branchInventoryRepository;
    private final CashRegisterRepository cashRegisterRepository;

    @Override
    public SalesSummaryResponse getSalesSummary(ReportFilterRequest filter) {
        LocalDate start = filter.getStartDate();
        LocalDate end = resolveEndDate(filter.getEndDate());
        PeriodSummaryProjection projection = saleRepository.findPeriodSummary(start, end, filter.getBranchId());
        Long count = projection != null ? projection.getTransactionCount() : 0L;
        BigDecimal revenue = projection != null && projection.getTotalRevenue() != null
                ? projection.getTotalRevenue()
                : BigDecimal.ZERO;
        return SalesSummaryResponse.builder()
                .totalRevenue(revenue)
                .transactionCount(count)
                .averageTicket(calculateAverage(revenue, count))
                .build();
    }

    @Override
    public List<SalesByBranchDTO> getSalesByBranch(ReportFilterRequest filter) {
        return saleRepository
                .findSalesGroupedByBranch(filter.getStartDate(), resolveEndDate(filter.getEndDate()), filter.getBranchId())
                .stream()
                .map(p -> SalesByBranchDTO.builder()
                        .branchId(p.getBranchId())
                        .branchName(p.getBranchName())
                        .totalRevenue(p.getTotalRevenue())
                        .transactionCount(p.getTransactionCount())
                        .build())
                .toList();
    }

    @Override
    public Page<SalesByProductDTO> getSalesByProduct(ReportFilterRequest filter, Pageable pageable) {
        return saleRepository
                .findSalesGroupedByProduct(
                        filter.getStartDate(), resolveEndDate(filter.getEndDate()),
                        filter.getBranchId(), filter.getProductId(), pageable)
                .map(p -> SalesByProductDTO.builder()
                        .productId(p.getProductId())
                        .productName(p.getProductName())
                        .productCategory(p.getProductCategory())
                        .totalQuantitySold(p.getTotalQuantitySold())
                        .totalRevenue(p.getTotalRevenue())
                        .build());
    }

    @Override
    public List<SalesByCashierDTO> getSalesByCashier(ReportFilterRequest filter) {
        return saleRepository
                .findSalesGroupedByCashier(
                        filter.getStartDate(), resolveEndDate(filter.getEndDate()),
                        filter.getBranchId(), filter.getCashierId())
                .stream()
                .map(p -> {
                    BigDecimal revenue = p.getTotalRevenue() != null ? p.getTotalRevenue() : BigDecimal.ZERO;
                    Long count = p.getTransactionCount();
                    return SalesByCashierDTO.builder()
                            .cashierId(p.getCashierId())
                            .cashierUsername(p.getCashierUsername())
                            .totalRevenue(revenue)
                            .transactionCount(count)
                            .averageTicket(calculateAverage(revenue, count))
                            .build();
                })
                .toList();
    }

    @Override
    public SalesComparisonResponse getSalesComparison(ReportFilterRequest filter) {
        LocalDate endDate = resolveEndDate(filter.getEndDate());
        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : endDate.minusDays(30);
        long daysDiff = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate previousStart = startDate.minusDays(daysDiff);
        LocalDate previousEnd = startDate.minusDays(1);
        PeriodSummaryProjection currentProjection = saleRepository.findPeriodSummary(startDate, endDate, filter.getBranchId());
        PeriodSummaryProjection previousProjection = saleRepository.findPeriodSummary(previousStart, previousEnd, filter.getBranchId());
        SalesComparisonResponse.PeriodSummary current = buildPeriodSummary(startDate, endDate, currentProjection);
        SalesComparisonResponse.PeriodSummary previous = buildPeriodSummary(previousStart, previousEnd, previousProjection);
        BigDecimal growth = previous.getTotalRevenue().compareTo(BigDecimal.ZERO) != 0
                ? current.getTotalRevenue()
                .subtract(previous.getTotalRevenue())
                .divide(previous.getTotalRevenue(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return SalesComparisonResponse.builder()
                .currentPeriod(current)
                .previousPeriod(previous)
                .growthPercentage(growth)
                .build();
    }

    @Override
    public InventoryStatusResponse getInventoryStatus(ReportFilterRequest filter) {
        InventoryStatusProjection projection = branchInventoryRepository.findInventoryStatus(filter.getBranchId());
        if (projection == null) {
            return InventoryStatusResponse.builder()
                    .totalProducts(0L)
                    .totalUnitsInStock(0L)
                    .totalInventoryValue(BigDecimal.ZERO)
                    .lowStockCount(0L)
                    .outOfStockCount(0L)
                    .build();
        }
        return InventoryStatusResponse.builder()
                .totalProducts(projection.getTotalProducts())
                .totalUnitsInStock(projection.getTotalUnitsInStock())
                .totalInventoryValue(projection.getTotalInventoryValue() != null ? projection.getTotalInventoryValue() : BigDecimal.ZERO)
                .lowStockCount(projection.getLowStockCount())
                .outOfStockCount(projection.getOutOfStockCount())
                .build();
    }

    @Override
    public Page<ProductPerformanceDTO> getProductPerformance(ReportFilterRequest filter, Pageable pageable) {
        return branchInventoryRepository
                .findProductPerformance(filter.getStartDate(), resolveEndDate(filter.getEndDate()), filter.getBranchId(), pageable)
                .map(p -> {
                    double turnover = p.getCurrentStock() != null && p.getCurrentStock() > 0
                            ? (double) p.getTotalSold() / p.getCurrentStock()
                            : 0.0;
                    return ProductPerformanceDTO.builder()
                            .productId(p.getProductId())
                            .productName(p.getProductName())
                            .productCategory(p.getProductCategory())
                            .totalSold(p.getTotalSold())
                            .currentStock(p.getCurrentStock())
                            .inventoryTurnoverRate(BigDecimal.valueOf(turnover).setScale(2, RoundingMode.HALF_UP))
                            .build();
                });
    }

    @Override
    public CashRegisterReportResponse getCashRegisterReport(CashRegisterFilterRequest filter, Pageable pageable) {
        Page<ClosureDiscrepancyProjection> page = cashRegisterRepository.findClosureDiscrepancies(
                filter.getStartDate(),
                resolveEndDate(filter.getEndDate()),
                filter.getBranchId(),
                filter.isShowOnlyDiscrepancies(),
                pageable);
        List<CashRegisterReportResponse.ClosureDiscrepancyDTO> discrepancies = page.getContent().stream()
                .map(p -> CashRegisterReportResponse.ClosureDiscrepancyDTO.builder()
                        .registerId(p.getRegisterId())
                        .branchId(p.getBranchId())
                        .branchName(p.getBranchName())
                        .openingTime(p.getOpeningTime())
                        .closingTime(p.getClosingTime())
                        .openedBy(p.getOpenedBy())
                        .closedBy(p.getClosedBy())
                        .expectedAmount(p.getExpectedAmount())
                        .actualClosingAmount(p.getActualClosingAmount())
                        .varianceAmount(p.getVarianceAmount())
                        .build())
                .toList();
        BigDecimal totalSurplus = discrepancies.stream()
                .map(CashRegisterReportResponse.ClosureDiscrepancyDTO::getVarianceAmount)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalShortage = discrepancies.stream()
                .map(CashRegisterReportResponse.ClosureDiscrepancyDTO::getVarianceAmount)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CashRegisterReportResponse.builder()
                .totalClosures(page.getTotalElements())
                .totalSurplus(totalSurplus)
                .totalShortage(totalShortage)
                .discrepancies(discrepancies)
                .build();
    }

    private LocalDate resolveEndDate(LocalDate endDate) {
        return endDate != null ? endDate : LocalDate.now();
    }

    private BigDecimal calculateAverage(BigDecimal total, Long count) {
        if (count == null || count == 0) return BigDecimal.ZERO;
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private SalesComparisonResponse.PeriodSummary buildPeriodSummary(LocalDate start, LocalDate end, PeriodSummaryProjection projection) {
        Long count = projection != null ? projection.getTransactionCount() : 0L;
        BigDecimal revenue = projection != null && projection.getTotalRevenue() != null
                ? projection.getTotalRevenue()
                : BigDecimal.ZERO;
        return SalesComparisonResponse.PeriodSummary.builder()
                .startDate(start)
                .endDate(end)
                .totalRevenue(revenue)
                .transactionCount(count)
                .averageTicket(calculateAverage(revenue, count))
                .build();
    }
}