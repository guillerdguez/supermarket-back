package com.supermarket.supermarket.repository;

import com.supermarket.supermarket.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByName(String name);

    boolean existsByBarcode(String barcode);

    @Query("""
            SELECT
                p.id       as productId,
                p.name     as productName,
                p.category as productCategory,
                COALESCE(SUM(sd.quantity), 0) as totalSold
            FROM Product p
            LEFT JOIN SaleDetail sd ON sd.product.id = p.id
            LEFT JOIN sd.sale s ON s.status = 'REGISTERED'
                AND (CAST(:startDate AS date) IS NULL OR s.date >= :startDate)
                AND (CAST(:endDate   AS date) IS NULL OR s.date <= :endDate)
                AND (:branchId IS NULL OR s.branch.id = :branchId)
            GROUP BY p.id, p.name, p.category
            ORDER BY totalSold DESC
            """)
    List<ProductSalesProjection> findProductSalesTotals(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("branchId") Long branchId);

    interface ProductSalesProjection {
        Long getProductId();

        String getProductName();

        String getProductCategory();

        Long getTotalSold();
    }
}
