package com.supermarket.supermarket.repository;


import com.supermarket.supermarket.model.branch.BranchInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface BranchInventoryRepository extends JpaRepository<BranchInventory, Long> {
    Optional<BranchInventory> findByBranchIdAndProductId(Long branchId, Long productId);

    List<BranchInventory> findByBranchId(Long branchId);

    boolean existsByBranchIdAndStockGreaterThan(Long branchId, Integer stock);

    @Modifying
    @Query("DELETE FROM BranchInventory bi WHERE bi.branch.id = :branchId")
    void deleteByBranchId(@Param("branchId") Long branchId);

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

    @Query("""
            SELECT
                bi.product.id as productId,
                SUM(bi.stock) as stock
            FROM BranchInventory bi
            WHERE (:branchId IS NULL OR bi.branch.id = :branchId)
            GROUP BY bi.product.id
            """)
    List<ProductStockProjection> findStockByProduct(@Param("branchId") Long branchId);

    interface InventoryStatusProjection {
        Long getTotalProducts();

        Long getTotalUnitsInStock();

        BigDecimal getTotalInventoryValue();

        Long getLowStockCount();

        Long getOutOfStockCount();
    }

    interface ProductStockProjection {
        Long getProductId();

        Long getStock();
    }
}