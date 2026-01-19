package com.supermarket.supermarket.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.exception.DuplicateResourceException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.ProductMapper;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.service.ProductService;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    @Override
    public List<ProductResponse> getAll() {
        log.info("Fetching all products");
        return productMapper.toResponseList(productRepository.findAll());
    }

    @Transactional(readOnly = true)
    @Override
    public ProductResponse getById(Long id) {
        return mapToDto(findProduct(id));
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        log.info("Creating new product: {}", request.getName());

        if (productRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Product already exists with name: " + request.getName());
        }

        Product product = productMapper.toEntity(request);
        return mapToDto(productRepository.save(product));
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProduct(id);
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            if (productRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Product name already in use: " + request.getName());
            }
        }
        productMapper.updateEntity(request, product);
        return mapToDto(productRepository.save(product));
    }

    @Override
    public void delete(Long id) {
        Product product = findProduct(id);
        productRepository.delete(product);
        log.info("Product deleted - ID: {}", id);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    private ProductResponse mapToDto(Product product) {
        return productMapper.toResponse(product);
    }
}