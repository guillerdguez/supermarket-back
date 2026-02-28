package com.supermarket.supermarket.repository;

import com.supermarket.supermarket.model.sale.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySaleId(Long saleId);
}