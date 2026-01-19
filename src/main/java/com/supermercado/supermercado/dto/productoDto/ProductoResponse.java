
package com.supermercado.supermercado.dto.productoDto;

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
public class ProductoResponse {
    
    private Long id;
    private String nombre;
    private String categoria;
    private Double precio;
    private Integer cantidad;
}