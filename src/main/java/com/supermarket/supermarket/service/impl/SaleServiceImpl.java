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
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        log.info("Fetching sale with ID: {} with fully optimized query", id);
        Sale sale = saleRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
        return saleMapper.toResponse(sale);
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

    private void processDetailsAndStock(Sale sale, List<SaleDetailRequest> detailsRequest) {
        if (CollectionUtils.isEmpty(detailsRequest)) {
            throw new IllegalArgumentException("La venta debe contener al menos un producto");
        }

        List<Product> updatedProducts = productService.validateAndReduceStockBatch(detailsRequest);

        Map<Long, Product> productMap = updatedProducts.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        BigDecimal saleTotal = BigDecimal.ZERO;
        List<SaleDetail> saleDetails = new ArrayList<>();

        for (SaleDetailRequest item : detailsRequest) {
            Product product = productMap.get(item.getProductId());

            SaleDetail detail = SaleDetail.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(product.getPrice())
                    .build();

            saleDetails.add(detail);

            BigDecimal lineSubtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            saleTotal = saleTotal.add(lineSubtotal);
        }

        sale.getDetails().addAll(saleDetails);
        sale.setTotal(saleTotal);
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

        if (sale.getStatus() == SaleStatus.REGISTERED && !CollectionUtils.isEmpty(sale.getDetails())) {
            productService.restoreStockBatch(sale.getDetails());
        }

        saleRepo.delete(sale);
        log.info("Sale deleted successfully - ID: {}", id);
    }

    private void updateDetailsAndStock(Sale sale, List<SaleDetailRequest> newDetails) {
        if (!CollectionUtils.isEmpty(sale.getDetails())) {
            productService.restoreStockBatch(sale.getDetails());
        }

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

    private SaleResponse mapToDto(Sale sale) {
        return saleMapper.toResponse(sale);
    }
}