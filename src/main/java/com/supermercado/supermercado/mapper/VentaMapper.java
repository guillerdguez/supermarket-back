package com.supermercado.supermercado.mapper;

import com.supermercado.supermercado.dto.ventaDto.VentaRequest;
import com.supermercado.supermercado.dto.ventaDto.VentaResponse;
import com.supermercado.supermercado.model.Venta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class VentaMapper {

    private final DetalleVentaMapper detalleVentaMapper;

    // 1. ENTIDAD → DTO (Lectura para el Frontend)
    public VentaResponse toResponse(Venta venta) {
        if (venta == null) return null;

        return VentaResponse.builder()
                .id(venta.getId())
                .fecha(venta.getFecha())
                .estado(venta.getEstado())
                .total(venta.getTotal())
                // Manejo seguro de la relación con Sucursal
                .idSucursal(venta.getSucursal() != null ? venta.getSucursal().getId() : null)
                .nombreSucursal(venta.getSucursal() != null ? venta.getSucursal().getNombre() : null)
                .detalles(detalleVentaMapper.toResponseList(venta.getDetalles()))
                .build();
    }

    // 2. DTO → ENTIDAD (Creación)
    public Venta toEntity(VentaRequest request) {
        if (request == null) return null;

        // Nota: No asignamos 'Sucursal' aquí porque requiere buscar en BD (tarea del Service).
        // Nota: No asignamos 'Total' ni 'Estado' porque son calculados por el sistema.

        return Venta.builder()
                .fecha(request.getFecha())
                .detalles(request.getDetalles() != null ?
                        request.getDetalles().stream()
                                .map(detalleVentaMapper::toEntity)
                                .collect(Collectors.toList())
                        : null)
                .build();
    }

    // 3. LISTAS
    public List<VentaResponse> toResponseList(List<Venta> ventas) {
        if (ventas == null) return null;

        return ventas.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}