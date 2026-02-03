package com.supermarket.supermarket.service.impl;

import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.SaleDetail;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.service.StockService;
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
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("The details list cannot be empty");
        }

        Map<Long, Integer> totalQuantities = new HashMap<>();
        for (SaleDetailRequest item : details) {
            if (item == null || item.getProductId() == null) {
                throw new IllegalArgumentException("Invalid detail: productId is required");
            }
            if (item.getStock() == null || item.getStock() <= 0) {
                throw new IllegalArgumentException("Invalid stock for productId: " + item.getProductId());
            }
            totalQuantities.merge(item.getProductId(), item.getStock(), Integer::sum);
        }

        List<Product> products = productRepository.findAllById(totalQuantities.keySet());

        if (products.size() != totalQuantities.size()) {
            Set<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            List<Long> missingIds = totalQuantities.keySet().stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new ResourceNotFoundException("Products not found with IDs: " + missingIds);
        }

        for (Product product : products) {
            Integer needed = totalQuantities.get(product.getId());
            if (product.getStock() == null || product.getStock() < needed) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for %s (id=%d). Requested: %d, Available: %d",
                                product.getName(), product.getId(), needed,
                                product.getStock() == null ? 0 : product.getStock()));
            }
            product.setStock(product.getStock() - needed);
        }
        return productRepository.saveAll(products);
    }

    @Override
    @Transactional
    public void restoreStockBatch(List<SaleDetail> existingDetails) {
    //revisar
        if (existingDetails == null || existingDetails.isEmpty()) {
            return;
        }
        Map<Long, Integer> quantitiesByProduct = existingDetails.stream()
                .map(saleDetail -> {
                    if (saleDetail.getProduct() == null || saleDetail.getProduct().getId() == null) {
                        throw new ResourceNotFoundException("SaleDetail contains null product or without id");
                    }
                    return saleDetail;
                })
                .collect(Collectors.groupingBy(saleDetail -> saleDetail.getProduct().getId(),
                        Collectors.summingInt(SaleDetail::getStock)));

        List<Product> products = productRepository.findAllById(quantitiesByProduct.keySet());

        if (products.size() != quantitiesByProduct.size()) {
            Set<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            List<Long> missingIds = quantitiesByProduct.keySet().stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new ResourceNotFoundException("Products not found: " + missingIds);
        }

        for (Product product : products) {
            Integer toRestore = quantitiesByProduct.get(product.getId());
            product.setStock((product.getStock() == null ? 0 : product.getStock()) + toRestore);
        }

        productRepository.saveAll(products);
    }
}