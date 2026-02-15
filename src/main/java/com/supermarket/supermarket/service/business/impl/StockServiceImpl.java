package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.SaleDetail;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.service.business.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public List<Product> validateAndReduceStockBatch(List<SaleDetailRequest> details) {
        log.info("Starting stock reduction for {} items", details.size());

        validateDetailsList(details);

        Map<Long, Integer> totalsByProduct = calculateTotalsByProduct(details);

        List<Product> products = findProductsWithValidation(totalsByProduct.keySet());

        verifyAvailableStock(products, totalsByProduct);

        reduceProductStock(products, totalsByProduct);

        return saveUpdatedProducts(products);
    }

    @Override
    @Transactional
    public void restoreStockBatch(List<SaleDetail> existingDetails) {
        if (existingDetails == null || existingDetails.isEmpty()) {
            log.debug("Empty list, nothing to restore");
            return;
        }

        log.info("Restoring stock for {} items", existingDetails.size());

        validateExistingDetails(existingDetails);

        Map<Long, Integer> quantitiesByProduct = extractQuantitiesByProduct(existingDetails);

        List<Product> products = findProductsWithValidation(quantitiesByProduct.keySet());

        restoreProductStock(products, quantitiesByProduct);

        saveUpdatedProducts(products);
    }

    private void validateDetailsList(List<SaleDetailRequest> details) {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Details list cannot be empty");
        }

        for (int i = 0; i < details.size(); i++) {
            SaleDetailRequest item = details.get(i);
            if (item == null || item.getProductId() == null) {
                throw new IllegalArgumentException(
                        String.format("Invalid detail at position %d: productId required", i)
                );
            }
            if (item.getStock() == null || item.getStock() <= 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid stock (%d) for productId: %d",
                                item.getStock(), item.getProductId())
                );
            }
        }
    }

    private Map<Long, Integer> calculateTotalsByProduct(List<SaleDetailRequest> details) {
        Map<Long, Integer> totals = new HashMap<>();
        for (SaleDetailRequest item : details) {
            totals.merge(item.getProductId(), item.getStock(), Integer::sum);
        }
        log.debug("Grouped {} details into {} products",
                details.size(), totals.size());
        return totals;
    }

    private List<Product> findProductsWithValidation(Set<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);

        if (products.size() != productIds.size()) {
            Set<Long> foundIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toSet());

            List<Long> missingIds = productIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .sorted()
                    .toList();

            throw new ResourceNotFoundException(
                    "Products not found with IDs: " + missingIds
            );
        }

        log.debug("Found {} products", products.size());
        return products;
    }

    private void verifyAvailableStock(
            List<Product> products,
            Map<Long, Integer> requiredQuantities
    ) {
        for (Product product : products) {
            Integer needed = requiredQuantities.get(product.getId());
            Integer available = product.getStock() == null ? 0 : product.getStock();

            if (available < needed) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for '%s' (ID: %d). " +
                                        "Required: %d, Available: %d",
                                product.getName(), product.getId(), needed, available)
                );
            }
        }
        log.debug("Stock verified for {} products", products.size());
    }

    private void reduceProductStock(
            List<Product> products,
            Map<Long, Integer> reductions
    ) {
        for (Product product : products) {
            Integer quantity = reductions.get(product.getId());
            Integer currentStock = product.getStock();
            product.setStock(currentStock - quantity);

            log.debug("Reduced {}: {} → {} (-{})",
                    product.getName(), currentStock, product.getStock(), quantity);
        }
    }

    private void restoreProductStock(
            List<Product> products,
            Map<Long, Integer> restorations
    ) {
        for (Product product : products) {
            Integer quantity = restorations.get(product.getId());
            Integer currentStock = product.getStock() == null ? 0 : product.getStock();
            product.setStock(currentStock + quantity);

            log.debug("Restored {}: {} → {} (+{})",
                    product.getName(), currentStock, product.getStock(), quantity);
        }
    }

    private List<Product> saveUpdatedProducts(List<Product> products) {
        List<Product> saved = productRepository.saveAll(products);
        log.info("Stock updated for {} products", saved.size());
        return saved;
    }

    private void validateExistingDetails(List<SaleDetail> details) {
        for (SaleDetail detail : details) {
            if (detail.getProduct() == null || detail.getProduct().getId() == null) {
                throw new ResourceNotFoundException(
                        "SaleDetail contains null product or without id"
                );
            }
        }
    }

    private Map<Long, Integer> extractQuantitiesByProduct(List<SaleDetail> details) {
        return details.stream()
                .collect(Collectors.groupingBy(
                        detail -> detail.getProduct().getId(),
                        Collectors.summingInt(SaleDetail::getStock)
                ));
    }
}