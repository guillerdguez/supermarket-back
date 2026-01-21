package com.supermarket.supermarket.service.impl;

import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.exception.DuplicateResourceException;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.ProductMapper;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<ProductResponse> getAll() {
        log.info("Fetching all products");
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
    public Product reduceStock(Long id, Integer quantity) {
        log.info("Reducing stock for Product ID: {} by {} units", id, quantity);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        int rowsAffected = productRepo.decrementStock(id, quantity);

        if (rowsAffected == 0) {
            Product product = findProduct(id);
            log.error("Insufficient stock for Product ID: {}. Available: {}", id, product.getQuantity());
            throw new InsufficientStockException(
                    "Insufficient stock for " + product.getName() + ". Available: " + product.getQuantity());
        }

        return findProduct(id);
    }

    @Override
    public void increaseStock(Long id, Integer quantity) {
        log.info("Restoring stock for Product ID: {} by {} units", id, quantity);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        productRepo.incrementStock(id, quantity);
    }

    private Product findProduct(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    private ProductResponse mapToDto(Product product) {
        return productMapper.toResponse(product);
    }
}