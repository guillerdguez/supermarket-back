package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.inventory.LowStockAlertResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.model.BranchInventory;
import com.supermarket.supermarket.model.SaleDetail;
import com.supermarket.supermarket.repository.BranchInventoryRepository;
import com.supermarket.supermarket.service.business.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final BranchInventoryRepository branchInventoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Integer getStockInBranch(Long branchId, Long productId) {
        return branchInventoryRepository.findByBranchIdAndProductId(branchId, productId)
                .map(BranchInventory::getStock)
                .orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockAlertResponse> getLowStockInBranch(Long branchId) {
        return branchInventoryRepository.findLowStockByBranchId(branchId).stream()
                .map(this::mapToLowStockAlert)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockAlertResponse> getLowStockGlobal() {
        return branchInventoryRepository.findLowStockGlobal().stream()
                .map(this::mapToLowStockAlert)
                .collect(Collectors.toList());
    }


    @Transactional
    public void validateAndReduceStock(Long branchId, Long productId, Integer quantity) {
        BranchInventory inventory = findInventory(branchId, productId);
        verifySufficientStock(inventory, quantity);
        inventory.setStock(inventory.getStock() - quantity);
        branchInventoryRepository.save(inventory);
        log.info("Reduced stock for product {} in branch {} by {}. New stock: {}",
                productId, branchId, quantity, inventory.getStock());
    }


    @Transactional
    public void restoreStock(Long branchId, Long productId, Integer quantity) {
        BranchInventory inventory = findInventory(branchId, productId);
        inventory.setStock(inventory.getStock() + quantity);
        branchInventoryRepository.save(inventory);
        log.info("Restored stock for product {} in branch {} by {}. New stock: {}",
                productId, branchId, quantity, inventory.getStock());
    }


    @Transactional
    public void validateAndReduceStockBatch(Long branchId, List<SaleDetailRequest> details) {
        log.info("Reducing batch stock for branch {} with {} items", branchId, details.size());

        validateDetailRequests(details);

        Map<Long, Integer> requiredQuantities = details.stream()
                .collect(Collectors.groupingBy(
                        SaleDetailRequest::getProductId,
                        Collectors.summingInt(SaleDetailRequest::getStock)
                ));

        List<BranchInventory> inventories = loadInventories(branchId, requiredQuantities.keySet());

        validateAllProductsExist(inventories, requiredQuantities.keySet());

        verifySufficientStockBatch(inventories, requiredQuantities);

        inventories.forEach(inv -> {
            int needed = requiredQuantities.get(inv.getProduct().getId());
            inv.setStock(inv.getStock() - needed);
        });

        branchInventoryRepository.saveAll(inventories);
        log.info("Stock reduced for {} products in branch {}", inventories.size(), branchId);
    }


    @Transactional
    public void restoreStockBatch(Long branchId, List<SaleDetail> details) {
        if (details == null || details.isEmpty()) {
            log.debug("Nothing to restore");
            return;
        }

        log.info("Restoring batch stock for branch {} with {} items", branchId, details.size());

        Map<Long, Integer> quantitiesToRestore = details.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getProduct().getId(),
                        Collectors.summingInt(SaleDetail::getStock)
                ));

        List<BranchInventory> inventories = loadInventories(branchId, quantitiesToRestore.keySet());

        validateAllProductsExist(inventories, quantitiesToRestore.keySet());

        inventories.forEach(inv -> {
            int restore = quantitiesToRestore.get(inv.getProduct().getId());
            inv.setStock(inv.getStock() + restore);
        });

        branchInventoryRepository.saveAll(inventories);
        log.info("Stock restored for {} products in branch {}", inventories.size(), branchId);
    }

    private BranchInventory findInventory(Long branchId, Long productId) {
        return branchInventoryRepository.findByBranchIdAndProductId(branchId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Product %d not found in branch %d", productId, branchId)
                ));
    }

    private void verifySufficientStock(BranchInventory inventory, Integer required) {
        if (inventory.getStock() < required) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for product %d in branch %d. Available: %d, required: %d",
                            inventory.getProduct().getId(), inventory.getBranch().getId(),
                            inventory.getStock(), required)
            );
        }
    }

    private void validateDetailRequests(List<SaleDetailRequest> details) {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Details list cannot be empty");
        }
        for (int i = 0; i < details.size(); i++) {
            SaleDetailRequest item = details.get(i);
            if (item.getProductId() == null) {
                throw new IllegalArgumentException("Detail at position " + i + " does not have a productId");
            }
            if (item.getStock() == null || item.getStock() <= 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid quantity (%d) for product %d",
                                item.getStock(), item.getProductId())
                );
            }
        }
    }

    private List<BranchInventory> loadInventories(Long branchId, Set<Long> productIds) {
        List<BranchInventory> inventories = branchInventoryRepository.findByBranchIdAndProductIdIn(branchId, productIds);
        if (inventories.size() != productIds.size()) {
            Set<Long> foundIds = inventories.stream()
                    .map(inv -> inv.getProduct().getId())
                    .collect(Collectors.toSet());
            List<Long> missing = productIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .sorted()
                    .toList();
            throw new ResourceNotFoundException(
                    String.format("The following products do not exist in branch %d: %s", branchId, missing)
            );
        }
        return inventories;
    }

    private void verifySufficientStockBatch(List<BranchInventory> inventories, Map<Long, Integer> required) {
        for (BranchInventory inv : inventories) {
            int needed = required.get(inv.getProduct().getId());
            if (inv.getStock() < needed) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for product '%s' (ID: %d) in branch %d. Available: %d, required: %d",
                                inv.getProduct().getName(), inv.getProduct().getId(),
                                inv.getBranch().getId(), inv.getStock(), needed)
                );
            }
        }
    }

    private void validateAllProductsExist(List<BranchInventory> inventories, Set<Long> requestedIds) {
        if (inventories.size() != requestedIds.size()) {
            Set<Long> foundIds = inventories.stream()
                    .map(inv -> inv.getProduct().getId())
                    .collect(Collectors.toSet());
            List<Long> missing = requestedIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .sorted()
                    .toList();
            throw new ResourceNotFoundException(
                    String.format("Products not found in branch: %s", missing)
            );
        }
    }

    private LowStockAlertResponse mapToLowStockAlert(BranchInventory inventory) {
        return LowStockAlertResponse.builder()
                .branchId(inventory.getBranch().getId())
                .branchName(inventory.getBranch().getName())
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .currentStock(inventory.getStock())
                .minStock(inventory.getMinStock())
                .build();
    }
}