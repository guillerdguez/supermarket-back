package com.supermercado.supermercado.dto.ventaDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.supermercado.supermercado.dto.detalleVentaDto.DetalleVentaRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
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
public class VentaRequest {

    // Opcional: Si no lo envían, el Service usa LocalDate.now()
    @PastOrPresent(message = "La fecha no puede ser futura")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @NotNull(message = "La sucursal es obligatoria")
    private Long idSucursal;

    @Valid
    @NotNull(message = "Los detalles de venta son obligatorios")
    @Size(min = 1, message = "Debe haber al menos un detalle de venta")
    private List<DetalleVentaRequest> detalles;
}