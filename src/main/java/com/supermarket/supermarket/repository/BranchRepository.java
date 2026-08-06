package com.supermarket.supermarket.repository;

import com.supermarket.supermarket.model.branch.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    Boolean existsByName(String name);

    Optional<Branch> findByIsWarehouseTrue();
}
