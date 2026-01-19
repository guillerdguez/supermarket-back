package com.supermercado.supermercado.mapper;

import com.supermercado.supermercado.dto.productoDto.ProductoRequest;
import com.supermercado.supermercado.dto.productoDto.ProductoResponse;
import com.supermercado.supermercado.model.Producto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductoMapper {

    // 1. ENTIDAD → DTO (Lectura)
    public ProductoResponse toResponse(Producto producto) {
        if (producto == null) return null;

        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .categoria(producto.getCategoria())
                .precio(producto.getPrecio())
                .cantidad(producto.getCantidad())
                .build();
    }

    // 2. DTO → ENTIDAD (Creación)
    public Producto toEntity(ProductoRequest request) {
        if (request == null) return null;

        return Producto.builder()
                .nombre(request.getNombre())
                .categoria(request.getCategoria())
                .precio(request.getPrecio())
                .cantidad(request.getCantidad())
                .build();
    }

    // 3. ACTUALIZACIÓN (Edición Profesional) 🌟
    // Transfiere datos del request a una entidad existente
    public void updateEntity(ProductoRequest request, Producto target) {
        if (request == null || target == null) return;

        // Solo actualizamos si el dato NO es nulo.
        // Esto permite actualizaciones parciales (PATCH).

        if (request.getNombre() != null) {
            target.setNombre(request.getNombre());
        }

        if (request.getCategoria() != null) {
            target.setCategoria(request.getCategoria());
        }

        if (request.getPrecio() != null) {
            target.setPrecio(request.getPrecio());
        }

        if (request.getCantidad() != null) {
            target.setCantidad(request.getCantidad());
        }

        // Nota: Jamás actualizamos el ID aquí.
    }

    // 4. LISTAS
    public List<ProductoResponse> toResponseList(List<Producto> entities) {
        if (entities == null) return null;

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}