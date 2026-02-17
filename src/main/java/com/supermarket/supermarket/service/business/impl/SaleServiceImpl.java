package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.InvalidSaleStateException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.SaleMapper;
import com.supermarket.supermarket.model.Branch;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.Sale;
import com.supermarket.supermarket.model.SaleDetail;
import com.supermarket.supermarket.model.SaleStatus;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.service.business.InventoryService;
import com.supermarket.supermarket.service.business.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepo;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final SaleMapper saleMapper;
    private final InventoryService inventoryService;

    @Override
    public SaleResponse create(SaleRequest request) {
        Sale sale = saleMapper.toEntity(request);
        sale.setStatus(SaleStatus.REGISTERED);
        sale.setDetails(new ArrayList<>());
        assignBranch(sale, request.getBranchId());

        inventoryService.validateAndReduceStockBatch(request.getBranchId(), request.getDetails());
        BigDecimal saleTotal = BigDecimal.ZERO;
        List<SaleDetail> saleDetails = new ArrayList<>();

        for (SaleDetailRequest detailRequest : request.getDetails()) {
            Product product = productRepository.findById(detailRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            SaleDetail saleDetail = SaleDetail.builder()
                    .sale(sale)
                    .product(product)
                    .stock(detailRequest.getStock())
                    .price(product.getPrice())
                    .build();

            saleDetails.add(saleDetail);
            saleTotal = saleTotal.add(product.getPrice().multiply(BigDecimal.valueOf(detailRequest.getStock())));
        }

        sale.getDetails().addAll(saleDetails);
        sale.setTotal(saleTotal);
        return saleMapper.toResponse(saleRepo.save(sale));
    }

    @Override
    public void delete(Long id) {
        Sale sale = saleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        if (sale.getStatus() == SaleStatus.REGISTERED && !CollectionUtils.isEmpty(sale.getDetails())) {
            inventoryService.restoreStockBatch(sale.getBranch().getId(), sale.getDetails());
        }
        saleRepo.delete(sale);
    }

    private void assignBranch(Sale sale, Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        sale.setBranch(branch);
    }

    @Override
    public List<SaleResponse> getAll() {
        return saleMapper.toResponseList(saleRepo.findAll());
    }

    @Override
    public SaleResponse getById(Long id) {
        Sale sale = saleRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
        return saleMapper.toResponse(sale);
    }

    @Override
    public SaleResponse update(Long id, SaleRequest request) {
        Sale existingSale = saleRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        if (existingSale.getStatus() == SaleStatus.CANCELLED) {
            throw new InvalidSaleStateException("Cannot update a cancelled sale");
        }

        if (!existingSale.getBranch().getId().equals(request.getBranchId())) {
            throw new InvalidOperationException("Cannot change branch of an existing sale");
        }

        if (!CollectionUtils.isEmpty(existingSale.getDetails())) {
            inventoryService.restoreStockBatch(existingSale.getBranch().getId(), existingSale.getDetails());
        }

        inventoryService.validateAndReduceStockBatch(request.getBranchId(), request.getDetails());

        existingSale.setDate(request.getDate());

        existingSale.getDetails().clear();

        BigDecimal saleTotal = BigDecimal.ZERO;
        List<SaleDetail> newDetails = new ArrayList<>();

        for (SaleDetailRequest detailRequest : request.getDetails()) {
            Product product = productRepository.findById(detailRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + detailRequest.getProductId()));

            SaleDetail saleDetail = SaleDetail.builder()
                    .sale(existingSale)
                    .product(product)
                    .stock(detailRequest.getStock())
                    .price(product.getPrice())
                    .build();

            newDetails.add(saleDetail);
            saleTotal = saleTotal.add(product.getPrice().multiply(BigDecimal.valueOf(detailRequest.getStock())));
        }

        existingSale.getDetails().addAll(newDetails);
        existingSale.setTotal(saleTotal);

        Sale updatedSale = saleRepo.save(existingSale);
        return saleMapper.toResponse(updatedSale);
    }
}