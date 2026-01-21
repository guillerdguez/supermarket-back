package com.supermarket.supermarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.supermarket.supermarket.model.Sale;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    boolean existsByBranchId(Long branchId);

    boolean existsByDetailsProductId(Long productId);
}