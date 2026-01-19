package com.supermercado.supermercado.service;


import com.supermercado.supermercado.dto.ventaDto.VentaRequest;
import com.supermercado.supermercado.dto.ventaDto.VentaResponse;

import java.util.List;

public interface VentaService {
    List<VentaResponse> getAll();

    VentaResponse getById(Long id);

    VentaResponse create(VentaRequest venta);

    VentaResponse update(Long id, VentaRequest venta);

    void delete(Long id);
}