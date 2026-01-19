package com.supermercado.supermercado.fixtures;

import com.supermercado.supermercado.dto.detalleVentaDto.DetalleVentaRequest;
import com.supermercado.supermercado.dto.detalleVentaDto.DetalleVentaResponse;
import com.supermercado.supermercado.dto.productoDto.ProductoRequest;
import com.supermercado.supermercado.dto.productoDto.ProductoResponse;
import com.supermercado.supermercado.dto.sucursalDto.SucursalRequest;
import com.supermercado.supermercado.dto.sucursalDto.SucursalResponse;
import com.supermercado.supermercado.dto.ventaDto.VentaRequest;
import com.supermercado.supermercado.dto.ventaDto.VentaResponse;
import com.supermercado.supermercado.model.DetalleVenta;
import com.supermercado.supermercado.model.Estado;
import com.supermercado.supermercado.model.Producto;
import com.supermercado.supermercado.model.Sucursal;
import com.supermercado.supermercado.model.Venta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TestFixtures {

    public static Producto defaultProduct() {
        return productWithIdAndStock(1L, 100);
    }

    public static Producto productWithIdAndStock(Long id, int stock) {
        return Producto.builder()
                .id(id)
                .nombre("Premium Rice")
                .categoria("Food")
                .precio(2.50)
                .cantidad(stock)
                .build();
    }

    public static ProductoRequest validProductRequest() {
        return ProductoRequest.builder()
                .nombre("New Product")
                .categoria("Cleaning")
                .precio(10.0)
                .cantidad(50)
                .build();
    }

    public static ProductoRequest invalidProductRequest() {
        return ProductoRequest.builder()
                .nombre("")
                .categoria("")
                .precio(-10.0)
                .cantidad(-5)
                .build();
    }

    public static ProductoResponse productResponse() {
        return ProductoResponse.builder()
                .id(1L)
                .nombre("Premium Rice")
                .categoria("Food")
                .precio(2.50)
                .cantidad(100)
                .build();
    }

    public static Sucursal defaultBranch() {
        return Sucursal.builder()
                .id(1L)
                .nombre("Central Branch")
                .direccion("123 Main St")
                .build();
    }

    public static SucursalRequest validBranchRequest() {
        return SucursalRequest.builder()
                .nombre("New Branch")
                .direccion("456 North Ave")
                .build();
    }

    public static SucursalRequest invalidBranchRequest() {
        return SucursalRequest.builder()
                .nombre("")
                .direccion("")
                .build();
    }

    public static SucursalResponse branchResponse() {
        return SucursalResponse.builder()
                .id(1L)
                .nombre("Central Branch")
                .direccion("123 Main St")
                .build();
    }

    public static VentaRequest validSaleRequest() {
        return VentaRequest.builder()
                .idSucursal(1L)
                .fecha(LocalDate.now())
                .detalles(List.of(
                        DetalleVentaRequest.builder()
                                .productoId(1L)
                                .cantidad(5)
                                .build()))
                .build();
    }

    public static VentaRequest saleRequestWithMultipleProducts() {
        return VentaRequest.builder()
                .idSucursal(1L)
                .fecha(LocalDate.now())
                .detalles(List.of(
                        DetalleVentaRequest.builder().productoId(1L).cantidad(2).build(),
                        DetalleVentaRequest.builder().productoId(2L).cantidad(3).build()))
                .build();
    }

    public static VentaRequest invalidSaleRequest() {
        return VentaRequest.builder()
                .idSucursal(null)
                .detalles(List.of())
                .build();
    }

    public static Venta saleWithDetails() {
        Venta venta = Venta.builder()
                .id(100L)
                .fecha(LocalDate.now())
                .total(12.50)
                .estado(Estado.REGISTRADA)
                .sucursal(defaultBranch())
                .detalles(new ArrayList<>())
                .build();
        DetalleVenta detalle = DetalleVenta.builder()
                .cantidad(5)
                .precio(2.50)
                .producto(defaultProduct())
                .venta(venta)
                .build();

        venta.getDetalles().add(detalle);
        return venta;
    }

    public static VentaResponse saleResponse() {
        return VentaResponse.builder()
                .id(100L)
                .total(12.50)
                .fecha(LocalDate.now())
                .estado(Estado.REGISTRADA)
                .idSucursal(1L)
                .nombreSucursal("Central Branch")
                .detalles(List.of(
                        DetalleVentaResponse.builder()
                                .nombreProducto("Premium Rice")
                                .cantidad(5)
                                .precioUnitario(2.50)
                                .subtotal(12.50)
                                .build()))
                .build();
    }
}