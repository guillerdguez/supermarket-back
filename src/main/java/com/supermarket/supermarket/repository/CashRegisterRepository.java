package com.supermarket.supermarket.repository;

import com.supermarket.supermarket.model.CashRegister;
import com.supermarket.supermarket.model.CashRegisterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CashRegisterRepository extends JpaRepository<CashRegister, Long> {
    Optional<CashRegister> findByBranchIdAndStatus(Long branchId, CashRegisterStatus status);
}