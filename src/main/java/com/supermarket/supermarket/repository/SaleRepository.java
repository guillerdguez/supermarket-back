package com.supermarket.supermarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.supermarket.supermarket.model.Sale;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    boolean existsByBranchId(Long branchId);

    boolean existsByDetailsProductId(Long productId);

    @Override
    @EntityGraph(attributePaths = { "branch" })
    List<Sale> findAll();

    @EntityGraph(attributePaths = { "branch", "details", "details.product" })
    Optional<Sale> findWithDetailsById(Long id);
}