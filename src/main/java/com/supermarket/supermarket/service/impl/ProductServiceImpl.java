package com.supermarket.supermarket.service.impl;

import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.exception.DuplicateResourceException;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.ProductMapper;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.SaleDetail;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final ProductMapper productMapper;
    private final SaleRepository saleRepo;

    @Transactional(readOnly = true)
    @Override
    public Page<ProductResponse> getAll(Specification<Product> spec, Pageable pageable) {
        log.info("Fetching products page: {} with filters", pageable.getPageNumber());

        Page<Product> productsPage = productRepo.findAll(spec, pageable);

        return productsPage.map(productMapper::toResponse);
    }

    @Override
    public List<ProductResponse> getAllForDropdown() {
        log.info("Fetching simple list of products");
        return productMapper.toResponseList(productRepo.findAll());
    }

    @Transactional(readOnly = true)
    @Override
    public ProductResponse getById(Long id) {
        log.info("Fetching product with ID: {}", id);
        return mapToDto(findProduct(id));
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        log.info("Creating new product: {}", request.getName());
        if (productRepo.existsByName(request.getName())) {
            throw new DuplicateResourceException("Product already exists with name: " + request.getName());
        }
        Product product = productMapper.toEntity(request);
        return mapToDto(productRepo.save(product));
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        log.info("Updating product with ID: {}", id);
        Product product = findProduct(id);

        if (request.getName() != null && !request.getName().equals(product.getName())) {
            if (productRepo.existsByName(request.getName())) {
                throw new DuplicateResourceException("Product name already in use: " + request.getName());
            }
        }

        productMapper.updateEntity(request, product);
        return mapToDto(productRepo.save(product));
    }

    @Override
    public void delete(Long id) {
        log.info("Attempting to delete product with ID: {}", id);
        Product product = findProduct(id);

        if (saleRepo.existsByDetailsProductId(id)) {
            throw new InvalidOperationException("Cannot delete product: It has associated sales records");
        }

        productRepo.delete(product);
        log.info("Product deleted successfully - ID: {}", id);
    }

    @Override
    @Transactional
    public List<Product> validateAndReduceStockBatch(List<SaleDetailRequest> details) {
        log.info("Processing batch stock reduction for {} items", details.size());

        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Details list cannot be empty");
        }

        Map<Long, Integer> totalQuantities = new HashMap<>();
        for (SaleDetailRequest item : details) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Invalid quantity for product ID: " + item.getProductId());
            }
            totalQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        List<Product> products = productRepo.findAllById(totalQuantities.keySet());

        if (products.size() != totalQuantities.size()) {
            Set<Long> foundIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toSet());

            List<Long> missingIds = totalQuantities.keySet().stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());

            throw new ResourceNotFoundException("Products not found with IDs: " + missingIds);
        }

        for (Product product : products) {
            Integer quantityNeeded = totalQuantities.get(product.getId());

            if (product.getQuantity() < quantityNeeded) {
                throw new InsufficientStockException(
                        String.format("Insufficient stock for %s. Requested: %d, Available: %d",
                                product.getName(), quantityNeeded, product.getQuantity()));
            }

            product.setQuantity(product.getQuantity() - quantityNeeded);
        }

        return productRepo.saveAll(products);
    }

    @Override
    @Transactional
    public void restoreStockBatch(List<SaleDetail> existingDetails) {
        if (existingDetails == null || existingDetails.isEmpty()) {
            log.warn("Attempting to restore stock with empty list");
            return;
        }

        log.info("Restoring stock for {} sale items", existingDetails.size());

        Map<Long, Integer> quantitiesByProduct = existingDetails.stream()
                .collect(Collectors.groupingBy(
                        detail -> detail.getProduct().getId(),
                        Collectors.summingInt(SaleDetail::getQuantity)));

        List<Product> products = productRepo.findAllById(quantitiesByProduct.keySet());

        if (products.size() != quantitiesByProduct.size()) {
            Set<Long> foundIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toSet());
            List<Long> missingIds = quantitiesByProduct.keySet().stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new ResourceNotFoundException("Products not found: " + missingIds);
        }

        products.forEach(product -> {
            Integer quantityToRestore = quantitiesByProduct.get(product.getId());
            product.setQuantity(product.getQuantity() + quantityToRestore);
            log.debug("Restored {} units of product {}",
                    quantityToRestore, product.getName());
        });

        productRepo.saveAll(products);

        log.info("Stock successfully restored for {} products", products.size());
    }

    private Product findProduct(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    private ProductResponse mapToDto(Product product) {
        return productMapper.toResponse(product);
    }
}