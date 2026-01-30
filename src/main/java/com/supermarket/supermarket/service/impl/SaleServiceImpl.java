package com.supermarket.supermarket.service.impl;

import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.exception.InvalidSaleStateException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.SaleMapper;
import com.supermarket.supermarket.model.Branch;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.Sale;
import com.supermarket.supermarket.model.SaleDetail;
import com.supermarket.supermarket.model.SaleStatus;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.service.SaleService;
import com.supermarket.supermarket.service.StockService;
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
    private final BranchRepository branchRepository;
    private final SaleMapper saleMapper;
    private final StockService stockService;

    @Transactional(readOnly = true)
    @Override
    public List<SaleResponse> getAll() {
        return saleMapper.toResponseList(saleRepo.findAll());
    }

    @Transactional(readOnly = true)
    @Override
    public SaleResponse getById(Long id) {
        Sale sale = saleRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
        return saleMapper.toResponse(sale);
    }

    @Override
    public SaleResponse create(SaleRequest request) {
        Sale sale = saleMapper.toEntity(request);
        sale.setStatus(SaleStatus.REGISTERED);
        sale.setDetails(new ArrayList<>());

        assignBranch(sale, request.getBranchId());
        processDetailsAndStock(sale, request.getDetails());

        return saleMapper.toResponse(saleRepo.save(sale));
    }

    private void processDetailsAndStock(Sale sale, List<SaleDetailRequest> detailsRequest) {
        if (CollectionUtils.isEmpty(detailsRequest)) {
            throw new IllegalArgumentException("Sale must contain at least one product");
        }

        List<Product> updatedProducts = stockService.validateAndReduceStockBatch(detailsRequest);

        Map<Long, Product> productMap = updatedProducts.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        BigDecimal saleTotal = BigDecimal.ZERO;
        List<SaleDetail> saleDetails = new ArrayList<>();

        for (SaleDetailRequest saleDetailRequest : detailsRequest) {
            Product product = productMap.get(saleDetailRequest.getProductId());
            SaleDetail saleDetail = SaleDetail.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(saleDetailRequest.getQuantity())
                    .price(product.getPrice())
                    .build();
            saleDetails.add(saleDetail);

            BigDecimal lineSubtotal = product.getPrice().multiply(BigDecimal.valueOf(saleDetailRequest.getQuantity()));
            saleTotal = saleTotal.add(lineSubtotal);
        }

        sale.getDetails().addAll(saleDetails);
        sale.setTotal(saleTotal);
    }

    @Override
    public SaleResponse update(Long id, SaleRequest request) {
        Sale sale = findSale(id);

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new InvalidSaleStateException("Cannot edit a Sale that has already been cancelled");
        }

        if (request.getDate() != null) {
            sale.setDate(request.getDate());
        }

        if (request.getBranchId() != null && !request.getBranchId().equals(sale.getBranch().getId())) {
            assignBranch(sale, request.getBranchId());
        }

        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            updateDetailsAndStock(sale, request.getDetails());
        }

        return saleMapper.toResponse(saleRepo.save(sale));
    }

    @Override
    public void delete(Long id) {
        Sale sale = findSale(id);
        if (sale.getStatus() == SaleStatus.REGISTERED && !CollectionUtils.isEmpty(sale.getDetails())) {
            stockService.restoreStockBatch(sale.getDetails());
        }
        saleRepo.delete(sale);
    }

    private void updateDetailsAndStock(Sale sale, List<SaleDetailRequest> newDetails) {
        if (!CollectionUtils.isEmpty(sale.getDetails())) {
            stockService.restoreStockBatch(sale.getDetails());
        }
        sale.getDetails().clear();
        processDetailsAndStock(sale, newDetails);
    }

    private Sale findSale(Long id) {
        return saleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
    }

    private void assignBranch(Sale sale, Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + branchId));
        sale.setBranch(branch);
    }
}