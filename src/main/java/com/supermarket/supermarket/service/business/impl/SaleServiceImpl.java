package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.sale.CancelSaleRequest;
import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.exception.InsufficientPermissionsException;
import com.supermarket.supermarket.exception.InvalidSaleStateException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.SaleMapper;
import com.supermarket.supermarket.model.Branch;
import com.supermarket.supermarket.model.CashRegister;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.Sale;
import com.supermarket.supermarket.model.SaleDetail;
import com.supermarket.supermarket.model.SaleStatus;
import com.supermarket.supermarket.model.User;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.security.SecurityUtils;
import com.supermarket.supermarket.service.business.CashRegisterService;
import com.supermarket.supermarket.service.business.InventoryService;
import com.supermarket.supermarket.service.business.NotificationEventService;
import com.supermarket.supermarket.service.business.SaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SaleServiceImpl implements SaleService {
    private final SaleRepository saleRepo;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final SaleMapper saleMapper;
    private final InventoryService inventoryService;
    private final SecurityUtils securityUtils;
    private final CashRegisterService cashRegisterService;
    private final NotificationEventService notificationEventService;

    @Override
    public SaleResponse create(SaleRequest request) {
        Sale sale = buildSale(request);
        List<SaleDetail> details = buildSaleDetails(request, sale);
        sale.getDetails().addAll(details);
        sale.setTotal(calculateTotal(details));
        Sale saved = saleRepo.save(sale);
        checkLowStockAfterSale(saved);
        return saleMapper.toResponse(saved);
    }

    private Sale buildSale(SaleRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        CashRegister cashRegister = cashRegisterService.getRegisterEntityByBranch(branch.getId());
        Sale sale = saleMapper.toEntity(request);
        sale.setStatus(SaleStatus.REGISTERED);
        sale.setDetails(new ArrayList<>());
        sale.setCreatedBy(currentUser);
        sale.setBranch(branch);
        sale.setCashRegister(cashRegister);
        return sale;
    }

    private List<SaleDetail> buildSaleDetails(SaleRequest request, Sale sale) {
        Set<Long> uniqueProductIds = request.getDetails().stream()
                .map(SaleDetailRequest::getProductId)
                .collect(Collectors.toSet());
        List<Product> products = productRepository.findAllById(uniqueProductIds);
        if (products.size() != uniqueProductIds.size()) {
            Set<Long> foundIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toSet());
            List<Long> missingIds = uniqueProductIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new ResourceNotFoundException("Products not found with IDs: " + missingIds);
        }
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        inventoryService.validateAndReduceStockBatch(request.getBranchId(), request.getDetails());
        return request.getDetails().stream()
                .map(detailRequest -> {
                    Product product = productMap.get(detailRequest.getProductId());
                    return SaleDetail.builder()
                            .sale(sale)
                            .product(product)
                            .quantity(detailRequest.getQuantity())
                            .price(product.getPrice())
                            .build();
                })
                .toList();
    }

    private BigDecimal calculateTotal(List<SaleDetail> details) {
        return details.stream()
                .map(detail -> detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void checkLowStockAfterSale(Sale sale) {
        try {
            for (SaleDetail detail : sale.getDetails()) {
                int remaining = inventoryService.getStockInBranch(
                        sale.getBranch().getId(), detail.getProduct().getId());
                if (remaining <= 10) {
                    notificationEventService.onLowStock(
                            sale.getBranch().getName(),
                            detail.getProduct().getName(),
                            remaining,
                            10);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to check low stock after sale {}: {}", sale.getId(), e.getMessage());
        }
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

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getAll() {
        return saleMapper.toResponseList(saleRepo.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getById(Long id) {
        Sale sale = saleRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
        return saleMapper.toResponse(sale);
    }

    @Override
    public SaleResponse cancel(Long id, CancelSaleRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        Sale sale = saleRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new InvalidSaleStateException("Sale is already cancelled");
        }
        if (!CollectionUtils.isEmpty(sale.getDetails())) {
            inventoryService.restoreStockBatch(sale.getBranch().getId(), sale.getDetails());
        }
        sale.setStatus(SaleStatus.CANCELLED);
        sale.setCancelledBy(currentUser);
        sale.setCancellationReason(request.getReason());
        sale.setCancelledAt(LocalDateTime.now());
        Sale saved = saleRepo.save(sale);
        notificationEventService.onSaleCancelled(saved);
        return saleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SaleResponse> getSalesByCashier(Long cashierId, Pageable pageable) {
        return saleRepo.findByCreatedById(cashierId, pageable)
                .map(saleMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getSaleByIdAndCashier(Long saleId, Long cashierId) {
        Sale sale = saleRepo.findWithDetailsById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + saleId));
        if (sale.getCreatedBy() == null || !sale.getCreatedBy().getId().equals(cashierId)) {
            throw new InsufficientPermissionsException("You are not allowed to view this sale");
        }
        return saleMapper.toResponse(sale);
    }
}