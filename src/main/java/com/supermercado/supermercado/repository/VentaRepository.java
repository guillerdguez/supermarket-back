package com.supermercado.supermercado.repository;

import com.supermercado.supermercado.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    boolean existsBySucursalId(Long sucursalId);
}