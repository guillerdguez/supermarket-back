package com.supermarket.supermarket.repository;


import com.supermarket.supermarket.model.BranchInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Long> {
    Optional<BranchInventory> findByBranchIdAndProductId(Long branchId, Long productId);

    List<BranchInventory> findByBranchId(Long branchId);

    List<BranchInventory> findByProductId(Long productId);

    @Query("SELECT bi FROM BranchInventory bi WHERE bi.branch.id = :branchId AND bi.stock <= bi.minStock")
    List<BranchInventory> findLowStockByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT bi FROM BranchInventory bi WHERE bi.stock <= bi.minStock")
    List<BranchInventory> findLowStockGlobal();

    List<BranchInventory> findByBranchIdAndProductIdIn(Long branchId, Set<Long> productIds);

    @Query("""
            SELECT
                COUNT(DISTINCT bi.product.id)             as totalProducts,
                SUM(bi.stock)                             as totalUnitsInStock,
                SUM(bi.stock * bi.product.price)          as totalInventoryValue,
                SUM(CASE WHEN bi.stock <= bi.minStock AND bi.stock > 0 THEN 1 ELSE 0 END) as lowStockCount,
                SUM(CASE WHEN bi.stock = 0 THEN 1 ELSE 0 END) as outOfStockCount
            FROM BranchInventory bi
            WHERE (:branchId IS NULL OR bi.branch.id = :branchId)
            """)
    InventoryStatusProjection findInventoryStatus(@Param("branchId") Long branchId);

    @Query(value = """
            SELECT
                p.id          as productId,
                p.name        as productName,
                p.category    as productCategory,
                COALESCE(SUM(sd.quantity), 0)  as totalSold,
                COALESCE(bi.stock, 0)       as currentStock
            FROM Product p
            LEFT JOIN BranchInventory bi ON bi.product.id = p.id
                AND (:branchId IS NULL OR bi.branch.id = :branchId)
            LEFT JOIN SaleDetail sd ON sd.product.id = p.id
            LEFT JOIN sd.sale s ON s.status = 'REGISTERED'
                AND (CAST(:startDate AS date) IS NULL OR s.date >= :startDate)
                AND (CAST(:endDate   AS date) IS NULL OR s.date <= :endDate)
            WHERE (:branchId IS NULL OR bi.branch.id = :branchId)
            GROUP BY p.id, p.name, p.category, bi.stock
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT p.id)
                    FROM Product p
                    LEFT JOIN BranchInventory bi ON bi.product.id = p.id
                        AND (:branchId IS NULL OR bi.branch.id = :branchId)
                    WHERE (:branchId IS NULL OR bi.branch.id = :branchId)
                    """)
    Page<ProductPerformanceProjection> findProductPerformance(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("branchId") Long branchId,
            Pageable pageable);

    interface InventoryStatusProjection {
        Long getTotalProducts();

        Long getTotalUnitsInStock();

        BigDecimal getTotalInventoryValue();

        Long getLowStockCount();

        Long getOutOfStockCount();
    }

    interface ProductPerformanceProjection {
        Long getProductId();

        String getProductName();

        String getProductCategory();

        Long getTotalSold();

        Integer getCurrentStock();
    }
}