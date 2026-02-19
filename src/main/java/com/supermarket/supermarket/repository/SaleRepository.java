package com.supermarket.supermarket.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @EntityGraph(attributePaths = {"branch", "createdBy"})
    List<Sale> findAll();

    @EntityGraph(attributePaths = {"branch", "details", "details.product", "createdBy"})
    Optional<Sale> findWithDetailsById(Long id);

    Page<Sale> findByCreatedById(Long cashierId, Pageable pageable);
}