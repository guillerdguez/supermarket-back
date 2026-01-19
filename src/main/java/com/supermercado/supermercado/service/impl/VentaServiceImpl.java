package com.supermercado.supermercado.service.impl;

import com.supermercado.supermercado.dto.detalleVentaDto.DetalleVentaRequest;
import com.supermercado.supermercado.dto.ventaDto.VentaRequest;
import com.supermercado.supermercado.dto.ventaDto.VentaResponse;
import com.supermercado.supermercado.exception.InsufficientStockException; // Nueva
import com.supermercado.supermercado.exception.InvalidSaleStateException; // Nueva
import com.supermercado.supermercado.exception.ResourceNotFoundException;
import com.supermercado.supermercado.mapper.VentaMapper;
import com.supermercado.supermercado.model.*;
import com.supermercado.supermercado.repository.ProductoRepository;
import com.supermercado.supermercado.repository.SucursalRepository;
import com.supermercado.supermercado.repository.VentaRepository;
import com.supermercado.supermercado.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class VentaServiceImpl implements VentaService {
    private final VentaRepository ventaRepo;
    private final SucursalRepository sucursalRepo;
    private final VentaMapper ventaMapper;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    @Override
    public List<VentaResponse> getAll() {
        return ventaMapper.toResponseList(ventaRepo.findAll());
    }

    @Transactional(readOnly = true)
    @Override
    public VentaResponse getById(Long id) {
        return mapearADto(cargarVenta(id));
    }

    @Override
    public VentaResponse create(VentaRequest request) {
        Venta venta = ventaMapper.toEntity(request);
        venta.setEstado(Estado.REGISTRADA);
        venta.setDetalles(new ArrayList<>());

        asignarSucursal(venta, request.getIdSucursal());
        procesarDetallesYStock(venta, request.getDetalles());

        return mapearADto(ventaRepo.save(venta));
    }

    @Override
    public VentaResponse update(Long id, VentaRequest request) {
        Venta venta = cargarVenta(id);

        if (venta.getEstado() == Estado.ANULADA) {
            throw new InvalidSaleStateException("No se puede editar una venta que ya ha sido anulada");
        }

        if (request.getFecha() != null)
            venta.setFecha(request.getFecha());

        if (request.getIdSucursal() != null && !request.getIdSucursal().equals(venta.getSucursal().getId())) {
            asignarSucursal(venta, request.getIdSucursal());
        }

        if (request.getDetalles() != null && !request.getDetalles().isEmpty()) {
            actualizarDetallesYStock(venta, request.getDetalles());
        }

        return mapearADto(ventaRepo.save(venta));
    }

    @Override
    public void delete(Long id) {
        Venta venta = cargarVenta(id);
        ventaRepo.delete(venta);
    }

    private Venta cargarVenta(Long id) {
        return ventaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
    }

    private void asignarSucursal(Venta venta, Long idSucursal) {
        Sucursal sucursal = sucursalRepo.findById(idSucursal)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + idSucursal));
        venta.setSucursal(sucursal);
    }

    private void procesarDetallesYStock(Venta venta, List<DetalleVentaRequest> detallesRequest) {
        double totalVenta = 0.0;

        for (DetalleVentaRequest item : detallesRequest) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Producto no encontrado ID: " + item.getProductoId()));

            if (producto.getCantidad() < item.getCantidad()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para " + producto.getNombre() + ". Disponible: " + producto.getCantidad());
            }

            producto.setCantidad(producto.getCantidad() - item.getCantidad());

            DetalleVenta detalle = DetalleVenta.builder()
                    .cantidad(item.getCantidad())
                    .precio(producto.getPrecio())
                    .producto(producto)
                    .venta(venta)
                    .build();

            venta.getDetalles().add(detalle);
            totalVenta += (detalle.getCantidad() * detalle.getPrecio());
        }
        venta.setTotal(totalVenta);
    }

    private void actualizarDetallesYStock(Venta venta, List<DetalleVentaRequest> nuevosDetalles) {
        for (DetalleVenta detalleAntiguo : venta.getDetalles()) {
            Producto producto = detalleAntiguo.getProducto();
            producto.setCantidad(producto.getCantidad() + detalleAntiguo.getCantidad());
        }
        venta.getDetalles().clear();
        procesarDetallesYStock(venta, nuevosDetalles);
    }

    private VentaResponse mapearADto(Venta venta) {
        return ventaMapper.toResponse(venta);
    }
}