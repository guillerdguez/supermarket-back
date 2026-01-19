package com.supermercado.supermercado.mapper;

import com.supermercado.supermercado.dto.sucursalDto.SucursalRequest;
import com.supermercado.supermercado.dto.sucursalDto.SucursalResponse;
import com.supermercado.supermercado.model.Sucursal;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SucursalMapper {

    // 1. ENTIDAD → DTO (Lectura)
    public SucursalResponse toResponse(Sucursal sucursal) {
        if (sucursal == null) return null;

        return SucursalResponse.builder()
                .id(sucursal.getId())
                .nombre(sucursal.getNombre())
                .direccion(sucursal.getDireccion())
                .build();
    }

    // 2. DTO → ENTIDAD (Creación)
    public Sucursal toEntity(SucursalRequest request) {
        if (request == null) return null;

        return Sucursal.builder()
                .nombre(request.getNombre())
                .direccion(request.getDireccion())
                .build();
    }


    // Transfiere datos del request a una entidad existente
    public void updateEntity(SucursalRequest request, Sucursal target) {
        if (request == null || target == null) return;

        if (request.getNombre() != null) {
            target.setNombre(request.getNombre());
        }

        if (request.getDireccion() != null) {
            target.setDireccion(request.getDireccion());
        }

    }

    // 4. LISTAS
    public List<SucursalResponse> toResponseList(List<Sucursal> sucursales) {
        if (sucursales == null) return null;

        return sucursales.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}