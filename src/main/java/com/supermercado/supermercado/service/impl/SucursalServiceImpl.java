package com.supermercado.supermercado.service.impl;

import com.supermercado.supermercado.dto.sucursalDto.SucursalRequest;
import com.supermercado.supermercado.dto.sucursalDto.SucursalResponse;
import com.supermercado.supermercado.exception.DuplicateResourceException;
import com.supermercado.supermercado.exception.InvalidOperationException;
import com.supermercado.supermercado.exception.ResourceNotFoundException;
import com.supermercado.supermercado.mapper.SucursalMapper;
import com.supermercado.supermercado.model.Sucursal;
import com.supermercado.supermercado.repository.SucursalRepository;
import com.supermercado.supermercado.repository.VentaRepository;
import com.supermercado.supermercado.service.SucursalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository sucursalRepo;
    private final SucursalMapper sucursalMapper;
    private final VentaRepository ventaRepo;

    @Transactional(readOnly = true)
    @Override
    public List<SucursalResponse> getAll() {
        log.info("Fetching all branches");
        return sucursalMapper.toResponseList(sucursalRepo.findAll());
    }

    @Transactional(readOnly = true)
    @Override
    public SucursalResponse getById(Long id) {
        log.info("Fetching branch with ID: {}", id);
        return mapearADto(cargarSucursal(id));
    }

    @Override
    public SucursalResponse create(SucursalRequest request) {
        log.info("Creating new branch: {}", request.getNombre());

        if (sucursalRepo.existsByNombre(request.getNombre())) {
            throw new DuplicateResourceException("Branch already exists with name: " + request.getNombre());
        }

        Sucursal sucursal = sucursalMapper.toEntity(request);
        return mapearADto(sucursalRepo.save(sucursal));
    }

    @Override
    public SucursalResponse update(Long id, SucursalRequest request) {
        log.info("Updating branch with ID: {}", id);
        Sucursal sucursal = cargarSucursal(id);

        if (request.getNombre() != null && !request.getNombre().equals(sucursal.getNombre())) {
            if (sucursalRepo.existsByNombre(request.getNombre())) {
                throw new DuplicateResourceException(
                        "Cannot update: Branch name '" + request.getNombre() + "' is already in use");
            }
        }

        sucursalMapper.updateEntity(request, sucursal);
        return mapearADto(sucursalRepo.save(sucursal));
    }

    @Override
    public void delete(Long id) {
        log.info("Attempting to delete branch with ID: {}", id);

        Sucursal sucursal = sucursalRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + id));

        if (ventaRepo.existsBySucursalId(id)) {
            throw new InvalidOperationException("Cannot delete branch: It has associated sales records");
        }

        sucursalRepo.delete(sucursal);
        log.info("Branch deleted successfully - ID: {}", id);
    }

    // Auxiliary Methods

    private Sucursal cargarSucursal(Long id) {
        return sucursalRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + id));
    }

    private SucursalResponse mapearADto(Sucursal sucursal) {
        return sucursalMapper.toResponse(sucursal);
    }
}