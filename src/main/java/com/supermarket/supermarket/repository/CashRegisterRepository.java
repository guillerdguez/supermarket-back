package com.supermarket.supermarket.repository;


import com.supermarket.supermarket.model.cashregister.CashRegister;
import com.supermarket.supermarket.model.cashregister.CashRegisterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CashRegisterRepository extends JpaRepository<CashRegister, Long> {
    Optional<CashRegister> findByBranchIdAndStatus(Long branchId, CashRegisterStatus status);

    @Query("""
            SELECT
                cr.id                        as registerId,
                cr.branch.id                 as branchId,
                cr.branch.name               as branchName,
                cr.openingTime               as openingTime,
                cr.closingTime               as closingTime,
                cr.openedBy.username         as openedBy,
                cr.closedBy.username         as closedBy,
                (cr.openingBalance + COALESCE((
                    SELECT SUM(s.total)
                    FROM Sale s
                    WHERE s.cashRegister.id = cr.id
                    AND s.status = 'REGISTERED'
                ), 0)) as expectedAmount,
                cr.closingBalance             as actualClosingAmount,
                (cr.closingBalance - (cr.openingBalance + COALESCE((
                    SELECT SUM(s.total)
                    FROM Sale s
                    WHERE s.cashRegister.id = cr.id
                    AND s.status = 'REGISTERED'
                ), 0))) as varianceAmount
            FROM CashRegister cr
            WHERE cr.status = 'CLOSED'
            AND (CAST(:startDate AS date) IS NULL OR DATE(cr.closingTime) >= :startDate)
            AND (CAST(:endDate   AS date) IS NULL OR DATE(cr.closingTime) <= :endDate)
            AND (:branchId IS NULL OR cr.branch.id = :branchId)
            AND (:showOnlyDiscrepancies = false OR
                (cr.closingBalance - (cr.openingBalance + COALESCE((
                    SELECT SUM(s.total)
                    FROM Sale s
                    WHERE s.cashRegister.id = cr.id
                    AND s.status = 'REGISTERED'
                ), 0))) <> 0)
            ORDER BY cr.closingTime DESC
            """)
    Page<ClosureDiscrepancyProjection> findClosureDiscrepancies(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("branchId") Long branchId,
            @Param("showOnlyDiscrepancies") boolean showOnlyDiscrepancies,
            Pageable pageable);

    interface ClosureDiscrepancyProjection {
        Long getRegisterId();

        Long getBranchId();

        String getBranchName();

        LocalDateTime getOpeningTime();

        LocalDateTime getClosingTime();

        String getOpenedBy();

        String getClosedBy();

        BigDecimal getExpectedAmount();

        BigDecimal getActualClosingAmount();

        BigDecimal getVarianceAmount();
    }
}