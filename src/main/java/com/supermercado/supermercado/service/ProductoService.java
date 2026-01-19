package com.supermercado.supermercado.service;

import com.supermercado.supermercado.dto.productoDto.ProductoRequest;
import com.supermercado.supermercado.dto.productoDto.ProductoResponse;

import java.util.List;

public interface ProductoService {
    List<ProductoResponse> getAll();

    ProductoResponse getById(Long id);

    ProductoResponse create(ProductoRequest producto);

    ProductoResponse update(Long id, ProductoRequest producto);

    void delete(Long id);
}