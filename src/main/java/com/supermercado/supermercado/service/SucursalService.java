package com.supermercado.supermercado.service;

import com.supermercado.supermercado.dto.sucursalDto.SucursalRequest;
import com.supermercado.supermercado.dto.sucursalDto.SucursalResponse;

import java.util.List;

public interface SucursalService {
    List<SucursalResponse> getAll();

    SucursalResponse getById(Long id);

    SucursalResponse create(SucursalRequest sucursal);

    SucursalResponse update(Long id, SucursalRequest sucursal);

    void delete(Long id);
}