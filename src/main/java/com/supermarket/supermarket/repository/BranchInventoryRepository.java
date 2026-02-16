package com.supermarket.supermarket.repository;

import com.supermarket.supermarket.model.BranchInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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
}