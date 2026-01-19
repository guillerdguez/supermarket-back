package com.supermarket.supermarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.supermarket.supermarket.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity - :quantity WHERE p.id = :productId AND p.quantity >= :quantity")
    int decrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity + :quantity WHERE p.id = :productId")
    int incrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}