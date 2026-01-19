package com.supermarket.supermarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.supermarket.supermarket.model.Branch;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Boolean existsByName(String name);
}