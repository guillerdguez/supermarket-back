package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.sale.CancelSaleRequest;
import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.exception.InsufficientPermissionsException;
import com.supermarket.supermarket.exception.InvalidSaleStateException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.SaleMapper;
import com.supermarket.supermarket.model.branch.Branch;
import com.supermarket.supermarket.model.cashregister.CashRegister;
import com.supermarket.supermarket.model.product.Product;
import com.supermarket.supermarket.model.sale.Payment;
import com.supermarket.supermarket.model.sale.Sale;
import com.supermarket.supermarket.model.sale.SaleDetail;
import com.supermarket.supermarket.model.sale.SaleStatus;
import com.supermarket.supermarket.model.user.User;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.PaymentRepository;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.security.SecurityUtils;
import com.supermarket.supermarket.service.business.CashRegisterService;
import com.supermarket.supermarket.service.business.InventoryService;
import com.supermarket.supermarket.service.business.NotificationEventService;
import com.supermarket.supermarket.service.business.SaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
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
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final SaleRepository saleRepo;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
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

        log.info("Sale created with id: {}, branch: {}, total: {}",
                saved.getId(),
                saved.getBranch().getId(),
                saved.getTotal());

        return saleMapper.toResponse(saved, List.of());
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


    @Override
    public void delete(Long id) {
        Sale sale = saleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));

        log.info("Attempting to delete sale with ID: {}", id);

        if (sale.getStatus() == SaleStatus.REGISTERED && !CollectionUtils.isEmpty(sale.getDetails())) {
            inventoryService.restoreStockBatch(sale.getBranch().getId(), sale.getDetails());
        }
        saleRepo.delete(sale);

        log.info("Sale deleted successfully - ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getAll() {
        List<Sale> sales = saleRepo.findAll(DEFAULT_SORT);
        Map<Long, List<Payment>> paymentsBySaleId = paymentsGroupedBySaleId(
                sales.stream().map(Sale::getId).toList());
        return sales.stream()
                .map(sale -> saleMapper.toResponse(sale, paymentsBySaleId.getOrDefault(sale.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getById(Long id) {
        Sale sale = saleRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
        return saleMapper.toResponse(sale, paymentRepository.findBySaleId(id));
    }

    private Map<Long, List<Payment>> paymentsGroupedBySaleId(List<Long> saleIds) {
        return paymentRepository.findBySaleIdIn(saleIds).stream()
                .collect(Collectors.groupingBy(payment -> payment.getSale().getId()));
    }

    @Override
    public SaleResponse cancel(Long id, CancelSaleRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        Sale sale = saleRepo.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new InvalidSaleStateException("Sale is already cancelled");
        }

        log.info("Cancelling sale id: {}", id);

        if (!CollectionUtils.isEmpty(sale.getDetails())) {
            inventoryService.restoreStockBatch(sale.getBranch().getId(), sale.getDetails());
        }

        sale.setStatus(SaleStatus.CANCELLED);
        sale.setCancelledBy(currentUser);
        sale.setCancellationReason(request.getReason());
        sale.setCancelledAt(LocalDateTime.now());

        Sale saved = saleRepo.save(sale);
        notificationEventService.onSaleCancelled(saved);

        log.info("Sale cancelled with id: {}", id);

        return saleMapper.toResponse(saved, paymentRepository.findBySaleId(saved.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getSalesByCashier(Long cashierId) {
        List<Sale> sales = saleRepo.findByCreatedById(cashierId, DEFAULT_SORT);
        Map<Long, List<Payment>> paymentsBySaleId = paymentsGroupedBySaleId(
                sales.stream().map(Sale::getId).toList());
        return sales.stream()
                .map(sale -> saleMapper.toResponse(sale, paymentsBySaleId.getOrDefault(sale.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getSaleByIdAndCashier(Long saleId, Long cashierId) {
        Sale sale = saleRepo.findWithDetailsById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + saleId));
        if (sale.getCreatedBy() == null || !sale.getCreatedBy().getId().equals(cashierId)) {
            throw new InsufficientPermissionsException("You are not allowed to view this sale");
        }
        return saleMapper.toResponse(sale, paymentRepository.findBySaleId(saleId));
    }


}