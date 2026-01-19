package com.supermercado.supermercado.service.impl;

import com.supermercado.supermercado.dto.productoDto.ProductoRequest;
import com.supermercado.supermercado.dto.productoDto.ProductoResponse;
import com.supermercado.supermercado.exception.DuplicateResourceException; // Nueva
import com.supermercado.supermercado.exception.ResourceNotFoundException;
import com.supermercado.supermercado.mapper.ProductoMapper;
import com.supermercado.supermercado.model.Producto;
import com.supermercado.supermercado.repository.ProductoRepository;
import com.supermercado.supermercado.service.ProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepo;
    private final ProductoMapper productoMapper;

    @Transactional(readOnly = true)
    @Override
    public List<ProductoResponse> getAll() {
        log.info("Obteniendo todos los productos");
        return productoMapper.toResponseList(productoRepo.findAll());
    }

    @Transactional(readOnly = true)
    @Override
    public ProductoResponse getById(Long id) {
        return mapearADto(cargarProducto(id));
    }

    @Override
    public ProductoResponse create(ProductoRequest request) {
        log.info("Creando nuevo producto: {}", request.getNombre());

        if (productoRepo.existsByNombre(request.getNombre())) {
            throw new DuplicateResourceException("Ya existe un producto con el nombre: " + request.getNombre());
        }

        Producto producto = productoMapper.toEntity(request);
        return mapearADto(productoRepo.save(producto));
    }

    @Override
    public ProductoResponse update(Long id, ProductoRequest productoRequest) {
        Producto producto = cargarProducto(id);
        if (productoRequest.getNombre() != null && !productoRequest.getNombre().equals(producto.getNombre())) {
            if (productoRepo.existsByNombre(productoRequest.getNombre())) {
                throw new DuplicateResourceException(
                        "Ya existe un producto con el nombre: " + productoRequest.getNombre());
            }
        }
        productoMapper.updateEntity(productoRequest, producto);
        return mapearADto(productoRepo.save(producto));
    }

    @Override
    public void delete(Long id) {
        Producto producto = cargarProducto(id);
        productoRepo.delete(producto);
        log.info("Producto eliminado - ID: {}", id);
    }

    private Producto cargarProducto(Long id) {
        return productoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
    }

    private ProductoResponse mapearADto(Producto producto) {
        return productoMapper.toResponse(producto);
    }
}