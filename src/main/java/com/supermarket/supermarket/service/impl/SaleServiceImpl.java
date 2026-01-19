package com.supermarket.supermarket.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.InvalidSaleStateException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.SaleMapper;
import com.supermarket.supermarket.model.*;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.service.SaleService;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class SaleServiceImpl implements SaleService {
    private final SaleRepository saleRepo;
    private final BranchRepository branchRepo;
    private final SaleMapper saleMapper;
    private final ProductRepository productRepo;

    @Transactional(readOnly = true)
    @Override
    public List<SaleResponse> getAll() {
        return saleMapper.toResponseList(saleRepo.findAll());
    }

    @Transactional(readOnly = true)
    @Override
    public SaleResponse getById(Long id) {
        return mapToDto(findSale(id));
    }

    @Override
    public SaleResponse create(SaleRequest request) {
        Sale sale = saleMapper.toEntity(request);
        sale.setStatus(SaleStatus.REGISTERED);
        sale.setDetails(new ArrayList<>());

        assignBranch(sale, request.getBranchId());
        processDetailsAndStock(sale, request.getDetails());

        return mapToDto(saleRepo.save(sale));
    }

    @Override
    public SaleResponse update(Long id, SaleRequest request) {
        Sale sale = findSale(id);

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new InvalidSaleStateException("Cannot edit a Sale that has already been cancelled");
        }

        if (request.getDate() != null)
            sale.setDate(request.getDate());

        if (request.getBranchId() != null && !request.getBranchId().equals(sale.getBranch().getId())) {
            assignBranch(sale, request.getBranchId());
        }

        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            updateDetailsAndStock(sale, request.getDetails());
        }

        return mapToDto(saleRepo.save(sale));
    }

    @Override
    public void delete(Long id) {
        Sale sale = findSale(id);
        saleRepo.delete(sale);
    }

    private Sale findSale(Long id) {
        return saleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
    }

    private void assignBranch(Sale sale, Long branchId) {
        Branch branch = branchRepo.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + branchId));
        sale.setBranch(branch);
    }

    private void processDetailsAndStock(Sale sale, List<SaleDetailRequest> detailsRequest) {
        double saleTotal = 0.0;

        for (SaleDetailRequest item : detailsRequest) {
            Product product = productRepo.findById(item.getProductId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Product not found ID: " + item.getProductId()));

            if (product.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for " + product.getName() + ". Available: " + product.getQuantity());
            }

            product.setQuantity(product.getQuantity() - item.getQuantity());

            SaleDetail detail = SaleDetail.builder()
                    .quantity(item.getQuantity())
                    .price(product.getPrice())
                    .product(product)
                    .sale(sale)
                    .build();

            sale.getDetails().add(detail);
            saleTotal += (detail.getQuantity() * detail.getPrice());
        }
        sale.setTotal(saleTotal);
    }

    private void updateDetailsAndStock(Sale sale, List<SaleDetailRequest> newDetails) {
        for (SaleDetail oldDetail : sale.getDetails()) {
            Product product = oldDetail.getProduct();
            product.setQuantity(product.getQuantity() + oldDetail.getQuantity());
        }
        sale.getDetails().clear();
        processDetailsAndStock(sale, newDetails);
    }

    private SaleResponse mapToDto(Sale sale) {
        return saleMapper.toResponse(sale);
    }
}