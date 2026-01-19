package com.supermercado.supermercado.dto.ventaDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.supermercado.supermercado.dto.detalleVentaDto.DetalleVentaResponse;
import com.supermercado.supermercado.model.Estado;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VentaResponse {

    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;
    private Estado estado;
    private Double total;
    private Long idSucursal;
    private String nombreSucursal; // Agregado útil para el frontend
    private List<DetalleVentaResponse> detalles;
}