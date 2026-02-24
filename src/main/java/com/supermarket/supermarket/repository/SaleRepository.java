package com.supermarket.supermarket.repository;

import com.supermarket.supermarket.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    boolean existsByBranchId(Long branchId);

    boolean existsByDetailsProductId(Long productId);

    @Override
    @EntityGraph(attributePaths = {"branch", "createdBy"})
    List<Sale> findAll();

    @EntityGraph(attributePaths = {"branch", "details", "details.product", "createdBy"})
    Optional<Sale> findWithDetailsById(Long id);

    Page<Sale> findByCreatedById(Long cashierId, Pageable pageable);

    @Query("""
            SELECT
                SUM(s.total)  as totalRevenue,
                COUNT(s.id)   as transactionCount
            FROM Sale s
            WHERE s.status = 'REGISTERED'
            AND (:startDate IS NULL OR s.date >= :startDate)
            AND (:endDate   IS NULL OR s.date <= :endDate)
            AND (:branchId  IS NULL OR s.branch.id = :branchId)
            """)
    PeriodSummaryProjection findPeriodSummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("branchId") Long branchId);

    @Query("""
            SELECT
                s.branch.id   as branchId,
                s.branch.name as branchName,
                SUM(s.total)  as totalRevenue,
                COUNT(s.id)   as transactionCount
            FROM Sale s
            WHERE s.status = 'REGISTERED'
            AND (:startDate IS NULL OR s.date >= :startDate)
            AND (:endDate   IS NULL OR s.date <= :endDate)
            AND (:branchId  IS NULL OR s.branch.id = :branchId)
            GROUP BY s.branch.id, s.branch.name
            ORDER BY totalRevenue DESC
            """)
    List<SalesByBranchProjection> findSalesGroupedByBranch(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("branchId") Long branchId);

    @Query("""
            SELECT
                p.id          as productId,
                p.name        as productName,
                p.category    as productCategory,
                 SUM(sd.quantity) as totalQuantitySold,
                 SUM(sd.price * sd.quantity) as totalRevenue
            FROM SaleDetail sd
            JOIN sd.product p
            JOIN sd.sale s
            WHERE s.status = 'REGISTERED'
            AND (:startDate IS NULL OR s.date >= :startDate)
            AND (:endDate   IS NULL OR s.date <= :endDate)
            AND (:branchId  IS NULL OR s.branch.id = :branchId)
            AND (:productId IS NULL OR p.id = :productId)
            GROUP BY p.id, p.name, p.category
            """)
    Page<SalesByProductProjection> findSalesGroupedByProduct(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("branchId") Long branchId,
            @Param("productId") Long productId,
            Pageable pageable);

    @Query("""
            SELECT
                u.id          as cashierId,
                u.username    as cashierUsername,
                SUM(s.total)  as totalRevenue,
                COUNT(s.id)   as transactionCount
            FROM Sale s
            JOIN s.createdBy u
            WHERE s.status = 'REGISTERED'
            AND (:startDate  IS NULL OR s.date >= :startDate)
            AND (:endDate    IS NULL OR s.date <= :endDate)
            AND (:branchId   IS NULL OR s.branch.id = :branchId)
            AND (:cashierId  IS NULL OR u.id = :cashierId)
            GROUP BY u.id, u.username
            ORDER BY totalRevenue DESC
            """)
    List<SalesByCashierProjection> findSalesGroupedByCashier(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("branchId") Long branchId,
            @Param("cashierId") Long cashierId);

    interface PeriodSummaryProjection {
        BigDecimal getTotalRevenue();

        Long getTransactionCount();
    }

    interface SalesByBranchProjection {
        Long getBranchId();

        String getBranchName();

        BigDecimal getTotalRevenue();

        Long getTransactionCount();
    }

    interface SalesByProductProjection {
        Long getProductId();

        String getProductName();

        String getProductCategory();

        Long getTotalQuantitySold();

        BigDecimal getTotalRevenue();
    }

    interface SalesByCashierProjection {
        Long getCashierId();

        String getCashierUsername();

        BigDecimal getTotalRevenue();

        Long getTransactionCount();
    }
}