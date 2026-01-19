package com.supermercado.supermercado.mapper;

import com.supermercado.supermercado.dto.detalleVentaDto.DetalleVentaRequest;
import com.supermercado.supermercado.dto.detalleVentaDto.DetalleVentaResponse;
import com.supermercado.supermercado.model.DetalleVenta;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DetalleVentaMapper {

    public DetalleVentaResponse toResponse(DetalleVenta entity) {
        if (entity == null) return null;

        double subtotal = 0.0;
        if (entity.getPrecio() != null && entity.getCantidad() != null) {
            subtotal = entity.getPrecio() * entity.getCantidad();
        }

        return DetalleVentaResponse.builder()
                .id(entity.getId())
                .cantidad(entity.getCantidad())
                .nombreProducto(entity.getProducto() != null ? entity.getProducto().getNombre() : null)
                .precioUnitario(entity.getPrecio())
                .subtotal(subtotal)
                .build();
    }

    public DetalleVenta toEntity(DetalleVentaRequest request) {
        if (request == null) return null;

        return DetalleVenta.builder()
                .cantidad(request.getCantidad())
                .build();
    }

    public List<DetalleVentaResponse> toResponseList(List<DetalleVenta> entities) {
        if (entities == null) return null;

        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}