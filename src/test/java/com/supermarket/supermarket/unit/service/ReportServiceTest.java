package com.supermarket.supermarket.unit.service;

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
import com.supermarket.supermarket.repository.BranchInventoryRepository.ProductPerformanceProjection;
import com.supermarket.supermarket.repository.CashRegisterRepository;
import com.supermarket.supermarket.repository.CashRegisterRepository.ClosureDiscrepancyProjection;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.repository.SaleRepository.PeriodSummaryProjection;
import com.supermarket.supermarket.repository.SaleRepository.SalesByBranchProjection;
import com.supermarket.supermarket.repository.SaleRepository.SalesByCashierProjection;
import com.supermarket.supermarket.repository.SaleRepository.SalesByProductProjection;
import com.supermarket.supermarket.service.business.impl.ReportServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private SaleRepository saleRepository;
    @Mock
    private BranchInventoryRepository branchInventoryRepository;
    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 1, 31);

    @Nested
    @DisplayName("getSalesSummary")
    class GetSalesSummary {
        @Test
        @DisplayName("should calculate average ticket from projection")
        void getSalesSummary_WithSales_ReturnsSummary() {
            PeriodSummaryProjection projection = mock(PeriodSummaryProjection.class);
            given(projection.getTransactionCount()).willReturn(10L);
            given(projection.getTotalRevenue()).willReturn(new BigDecimal("1000"));
            given(saleRepository.findPeriodSummary(START, END, 1L)).willReturn(projection);

            ReportFilterRequest filter = ReportFilterRequest.builder()
                    .startDate(START).endDate(END).branchId(1L).build();

            SalesSummaryResponse result = reportService.getSalesSummary(filter);

            assertThat(result.getTransactionCount()).isEqualTo(10L);
            assertThat(result.getTotalRevenue()).isEqualByComparingTo("1000");
            assertThat(result.getAverageTicket()).isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("should return zeros when no sales in period")
        void getSalesSummary_NoSales_ReturnsZeros() {
            given(saleRepository.findPeriodSummary(any(), any(), any())).willReturn(null);

            ReportFilterRequest filter = ReportFilterRequest.builder().build();

            SalesSummaryResponse result = reportService.getSalesSummary(filter);

            assertThat(result.getTransactionCount()).isZero();
            assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getAverageTicket()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getSalesByBranch")
    class GetSalesByBranch {
        @Test
        @DisplayName("should map grouped projections to DTOs")
        void getSalesByBranch_ReturnsMappedList() {
            SalesByBranchProjection projection = mock(SalesByBranchProjection.class);
            given(projection.getBranchId()).willReturn(1L);
            given(projection.getBranchName()).willReturn("Main Branch");
            given(projection.getTotalRevenue()).willReturn(new BigDecimal("500"));
            given(projection.getTransactionCount()).willReturn(5L);
            given(saleRepository.findSalesGroupedByBranch(any(), any(), any())).willReturn(List.of(projection));

            List<SalesByBranchDTO> result = reportService.getSalesByBranch(ReportFilterRequest.builder().build());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getBranchId()).isEqualTo(1L);
            assertThat(result.get(0).getBranchName()).isEqualTo("Main Branch");
            assertThat(result.get(0).getTotalRevenue()).isEqualByComparingTo("500");
            assertThat(result.get(0).getTransactionCount()).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("getSalesByProduct")
    class GetSalesByProduct {
        @Test
        @DisplayName("should map a page of grouped projections to DTOs")
        void getSalesByProduct_ReturnsMappedPage() {
            SalesByProductProjection projection = mock(SalesByProductProjection.class);
            given(projection.getProductId()).willReturn(10L);
            given(projection.getProductName()).willReturn("Milk");
            given(projection.getProductCategory()).willReturn("Dairy");
            given(projection.getTotalQuantitySold()).willReturn(20L);
            given(projection.getTotalRevenue()).willReturn(new BigDecimal("200"));

            Pageable pageable = PageRequest.of(0, 10);
            Page<SalesByProductProjection> page = new PageImpl<>(List.of(projection), pageable, 1);
            given(saleRepository.findSalesGroupedByProduct(any(), any(), any(), any(), eq(pageable))).willReturn(page);

            Page<SalesByProductDTO> result = reportService.getSalesByProduct(ReportFilterRequest.builder().build(), pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getProductId()).isEqualTo(10L);
            assertThat(result.getContent().get(0).getProductName()).isEqualTo("Milk");
            assertThat(result.getContent().get(0).getTotalQuantitySold()).isEqualTo(20L);
        }
    }

    @Nested
    @DisplayName("getSalesByCashier")
    class GetSalesByCashier {
        @Test
        @DisplayName("should map grouped projections to DTOs with average ticket")
        void getSalesByCashier_ReturnsMappedList() {
            SalesByCashierProjection projection = mock(SalesByCashierProjection.class);
            given(projection.getCashierId()).willReturn(2L);
            given(projection.getCashierUsername()).willReturn("cashier1");
            given(projection.getTotalRevenue()).willReturn(new BigDecimal("300"));
            given(projection.getTransactionCount()).willReturn(3L);
            given(saleRepository.findSalesGroupedByCashier(any(), any(), any(), any())).willReturn(List.of(projection));

            List<SalesByCashierDTO> result = reportService.getSalesByCashier(ReportFilterRequest.builder().build());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCashierUsername()).isEqualTo("cashier1");
            assertThat(result.get(0).getAverageTicket()).isEqualByComparingTo("100.00");
        }
    }

    @Nested
    @DisplayName("getSalesComparison")
    class GetSalesComparison {
        @Test
        @DisplayName("should calculate growth percentage between periods")
        void getSalesComparison_WithPreviousRevenue_CalculatesGrowth() {
            PeriodSummaryProjection current = mock(PeriodSummaryProjection.class);
            given(current.getTransactionCount()).willReturn(10L);
            given(current.getTotalRevenue()).willReturn(new BigDecimal("1500"));

            PeriodSummaryProjection previous = mock(PeriodSummaryProjection.class);
            given(previous.getTransactionCount()).willReturn(8L);
            given(previous.getTotalRevenue()).willReturn(new BigDecimal("1000"));

            given(saleRepository.findPeriodSummary(any(), any(), any())).willReturn(current, previous);

            ReportFilterRequest filter = ReportFilterRequest.builder().startDate(START).endDate(END).build();
            SalesComparisonResponse result = reportService.getSalesComparison(filter);

            assertThat(result.getCurrentPeriod().getTotalRevenue()).isEqualByComparingTo("1500");
            assertThat(result.getPreviousPeriod().getTotalRevenue()).isEqualByComparingTo("1000");
            assertThat(result.getGrowthPercentage()).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("should return zero growth when previous period had no revenue")
        void getSalesComparison_NoPreviousRevenue_ReturnsZeroGrowth() {
            PeriodSummaryProjection current = mock(PeriodSummaryProjection.class);
            given(current.getTransactionCount()).willReturn(5L);
            given(current.getTotalRevenue()).willReturn(new BigDecimal("500"));

            given(saleRepository.findPeriodSummary(any(), any(), any())).willReturn(current, null);

            ReportFilterRequest filter = ReportFilterRequest.builder().startDate(START).endDate(END).build();
            SalesComparisonResponse result = reportService.getSalesComparison(filter);

            assertThat(result.getPreviousPeriod().getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getGrowthPercentage()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getInventoryStatus")
    class GetInventoryStatus {
        @Test
        @DisplayName("should map projection to response")
        void getInventoryStatus_WithData_ReturnsMapped() {
            InventoryStatusProjection projection = mock(InventoryStatusProjection.class);
            given(projection.getTotalProducts()).willReturn(50L);
            given(projection.getTotalUnitsInStock()).willReturn(500L);
            given(projection.getTotalInventoryValue()).willReturn(new BigDecimal("12345.67"));
            given(projection.getLowStockCount()).willReturn(3L);
            given(projection.getOutOfStockCount()).willReturn(1L);
            given(branchInventoryRepository.findInventoryStatus(1L)).willReturn(projection);

            InventoryStatusResponse result = reportService.getInventoryStatus(
                    ReportFilterRequest.builder().branchId(1L).build());

            assertThat(result.getTotalProducts()).isEqualTo(50L);
            assertThat(result.getLowStockCount()).isEqualTo(3L);
            assertThat(result.getTotalInventoryValue()).isEqualByComparingTo("12345.67");
        }

        @Test
        @DisplayName("should return zeros when projection is null")
        void getInventoryStatus_NullProjection_ReturnsZeros() {
            given(branchInventoryRepository.findInventoryStatus(any())).willReturn(null);

            InventoryStatusResponse result = reportService.getInventoryStatus(ReportFilterRequest.builder().build());

            assertThat(result.getTotalProducts()).isZero();
            assertThat(result.getTotalUnitsInStock()).isZero();
            assertThat(result.getTotalInventoryValue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getLowStockCount()).isZero();
            assertThat(result.getOutOfStockCount()).isZero();
        }
    }

    @Nested
    @DisplayName("getProductPerformance")
    class GetProductPerformance {
        @Test
        @DisplayName("should calculate inventory turnover rate")
        void getProductPerformance_WithStock_CalculatesTurnover() {
            ProductPerformanceProjection projection = mock(ProductPerformanceProjection.class);
            given(projection.getProductId()).willReturn(1L);
            given(projection.getProductName()).willReturn("Milk");
            given(projection.getProductCategory()).willReturn("Dairy");
            given(projection.getTotalSold()).willReturn(50L);
            given(projection.getCurrentStock()).willReturn(25);

            Pageable pageable = PageRequest.of(0, 10);
            Page<ProductPerformanceProjection> page = new PageImpl<>(List.of(projection), pageable, 1);
            given(branchInventoryRepository.findProductPerformance(any(), any(), any(), eq(pageable))).willReturn(page);

            Page<ProductPerformanceDTO> result = reportService.getProductPerformance(
                    ReportFilterRequest.builder().build(), pageable);

            assertThat(result.getContent().get(0).getInventoryTurnoverRate()).isEqualByComparingTo("2.00");
        }

        @Test
        @DisplayName("should return zero turnover when current stock is zero")
        void getProductPerformance_NoStock_ReturnsZeroTurnover() {
            ProductPerformanceProjection projection = mock(ProductPerformanceProjection.class);
            given(projection.getTotalSold()).willReturn(10L);
            given(projection.getCurrentStock()).willReturn(0);

            Pageable pageable = PageRequest.of(0, 10);
            Page<ProductPerformanceProjection> page = new PageImpl<>(List.of(projection), pageable, 1);
            given(branchInventoryRepository.findProductPerformance(any(), any(), any(), eq(pageable))).willReturn(page);

            Page<ProductPerformanceDTO> result = reportService.getProductPerformance(
                    ReportFilterRequest.builder().build(), pageable);

            assertThat(result.getContent().get(0).getInventoryTurnoverRate()).isEqualByComparingTo("0.00");
        }
    }

    @Nested
    @DisplayName("getCashRegisterReport")
    class GetCashRegisterReport {
        @Test
        @DisplayName("should sum surplus and shortage separately, ignoring null variances")
        void getCashRegisterReport_MixedVariances_SumsCorrectly() {
            ClosureDiscrepancyProjection surplus = mock(ClosureDiscrepancyProjection.class);
            given(surplus.getVarianceAmount()).willReturn(new BigDecimal("50.00"));

            ClosureDiscrepancyProjection shortage = mock(ClosureDiscrepancyProjection.class);
            given(shortage.getVarianceAmount()).willReturn(new BigDecimal("-30.00"));

            ClosureDiscrepancyProjection noVariance = mock(ClosureDiscrepancyProjection.class);
            given(noVariance.getVarianceAmount()).willReturn(null);

            Pageable pageable = PageRequest.of(0, 10);
            Page<ClosureDiscrepancyProjection> page =
                    new PageImpl<>(List.of(surplus, shortage, noVariance), pageable, 3);
            given(cashRegisterRepository.findClosureDiscrepancies(any(), any(), any(), eq(false), eq(pageable)))
                    .willReturn(page);

            CashRegisterFilterRequest filter = CashRegisterFilterRequest.builder().showOnlyDiscrepancies(false).build();
            CashRegisterReportResponse result = reportService.getCashRegisterReport(filter, pageable);

            assertThat(result.getTotalClosures()).isEqualTo(3L);
            assertThat(result.getDiscrepancies()).hasSize(3);
            assertThat(result.getTotalSurplus()).isEqualByComparingTo("50.00");
            assertThat(result.getTotalShortage()).isEqualByComparingTo("-30.00");
        }
    }
}
