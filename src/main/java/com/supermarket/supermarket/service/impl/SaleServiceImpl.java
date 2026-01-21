package com.supermarket.supermarket.service.impl;

import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.exception.InvalidSaleStateException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.SaleMapper;
import com.supermarket.supermarket.model.*;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.service.ProductService;
import com.supermarket.supermarket.service.SaleService;
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
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepo;
    private final BranchRepository branchRepo;
    private final SaleMapper saleMapper;
    private final ProductService productService;

    @Transactional(readOnly = true)
    @Override
    public List<SaleResponse> getAll() {
        log.info("Fetching all sales");
        return saleMapper.toResponseList(saleRepo.findAll());
    }

    @Transactional(readOnly = true)
    @Override
    public SaleResponse getById(Long id) {
        log.info("Fetching sale with ID: {}", id);
        return mapToDto(findSale(id));
    }

    @Override
    public SaleResponse create(SaleRequest request) {
        log.info("Creating new sale for branch ID: {}", request.getBranchId());

        Sale sale = saleMapper.toEntity(request);
        sale.setStatus(SaleStatus.REGISTERED);
        sale.setDetails(new ArrayList<>());

        assignBranch(sale, request.getBranchId());
        processDetailsAndStock(sale, request.getDetails());

        return mapToDto(saleRepo.save(sale));
    }

    @Override
    public SaleResponse update(Long id, SaleRequest request) {
        log.info("Updating sale with ID: {}", id);
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
        log.info("Attempting to delete sale with ID: {}", id);
        Sale sale = findSale(id);

        if (sale.getStatus() == SaleStatus.REGISTERED) {
            restoreStock(sale);
        }

        saleRepo.delete(sale);
        log.info("Sale deleted successfully - ID: {}", id);
    }

    private void restoreStock(Sale sale) {
        for (SaleDetail detail : sale.getDetails()) {
            productService.increaseStock(
                    detail.getProduct().getId(),
                    detail.getQuantity());
        }
    }

    private void processDetailsAndStock(Sale sale, List<SaleDetailRequest> detailsRequest) {
        double saleTotal = 0.0;

        for (SaleDetailRequest item : detailsRequest) {
            Product product = productService.reduceStock(item.getProductId(), item.getQuantity());

            SaleDetail detail = buildSaleDetail(sale, item, product);
            sale.getDetails().add(detail);
            saleTotal += (detail.getQuantity() * detail.getPrice());
        }
        sale.setTotal(saleTotal);
    }

    private void updateDetailsAndStock(Sale sale, List<SaleDetailRequest> newDetails) {
        restoreStock(sale);

        sale.getDetails().clear();

        processDetailsAndStock(sale, newDetails);
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

    private SaleDetail buildSaleDetail(Sale sale, SaleDetailRequest item, Product product) {
        return SaleDetail.builder()
                .quantity(item.getQuantity())
                .price(product.getPrice())
                .product(product)
                .sale(sale)
                .build();
    }

    private SaleResponse mapToDto(Sale sale) {
        return saleMapper.toResponse(sale);
    }
}