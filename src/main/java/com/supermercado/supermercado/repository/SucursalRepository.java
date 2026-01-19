package com.supermercado.supermercado.repository;

import com.supermercado.supermercado.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    Boolean existsByNombre(String name);
}