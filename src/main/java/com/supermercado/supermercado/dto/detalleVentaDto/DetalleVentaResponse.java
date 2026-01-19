package com.supermercado.supermercado.dto.detalleVentaDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetalleVentaResponse {
    private Long id;
    private Integer cantidad;
    private String nombreProducto;
    private Double precioUnitario;
    private Double subtotal;
}