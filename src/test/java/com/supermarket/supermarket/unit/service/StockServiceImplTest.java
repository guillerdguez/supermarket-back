package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.fixtures.TestFixtures;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.SaleDetail;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.service.impl.StockServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private StockServiceImpl stockService;

    @Test
    @DisplayName("validateAndReduceStockBatch - should reduce stock when sufficient")
    void validateAndReduceStockBatch_ShouldReduceStock() {
        Long productId = 1L;
        int initialStock = 100;
        int quantity = 5;
        Product product = TestFixtures.productWithIdAndStock(productId, initialStock);
        SaleDetailRequest detail = TestFixtures.saleDetailRequest(productId, quantity);
        List<SaleDetailRequest> details = List.of(detail);
        given(productRepository.findAllById(anySet())).willReturn(List.of(product));
        given(productRepository.saveAll(anyList())).willReturn(List.of(product));
        List<Product> result = stockService.validateAndReduceStockBatch(details);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(initialStock - quantity);
        then(productRepository).should().findAllById(Set.of(productId));
        then(productRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("validateAndReduceStockBatch - should throw InsufficientStockException when stock is insufficient")
    void validateAndReduceStockBatch_WhenInsufficientStock_ShouldThrowException() {
        Long productId = 1L;
        int initialStock = 10;
        int quantity = 20;
        Product product = TestFixtures.productWithIdAndStock(productId, initialStock);
        SaleDetailRequest detail = TestFixtures.saleDetailRequest(productId, quantity);
        List<SaleDetailRequest> details = List.of(detail);

        given(productRepository.findAllById(anySet())).willReturn(List.of(product));

        assertThatThrownBy(() -> stockService.validateAndReduceStockBatch(details))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock"); 
        then(productRepository).should(never()).saveAll(anyList());
    }

    @Test
    @DisplayName("validateAndReduceStockBatch - should handle multiple products with same id")
    void validateAndReduceStockBatch_WithMultipleSameProduct_ShouldAggregateQuantities() {
        Long productId = 1L;
        int initialStock = 100;
        Product product = TestFixtures.productWithIdAndStock(productId, initialStock);
        SaleDetailRequest detail1 = TestFixtures.saleDetailRequest(productId, 5);
        SaleDetailRequest detail2 = TestFixtures.saleDetailRequest(productId, 10);
        List<SaleDetailRequest> details = List.of(detail1, detail2);
        given(productRepository.findAllById(anySet())).willReturn(List.of(product));
        given(productRepository.saveAll(anyList())).willReturn(List.of(product));
        List<Product> result = stockService.validateAndReduceStockBatch(details);
        assertThat(result.get(0).getQuantity()).isEqualTo(initialStock - 15);
    }

    @Test
    @DisplayName("validateAndReduceStockBatch - should throw when product not found")
    void validateAndReduceStockBatch_WhenProductNotFound_ShouldThrowException() {
        Long productId = 999L;
        SaleDetailRequest detail = TestFixtures.saleDetailRequest(productId, 5);
        List<SaleDetailRequest> details = List.of(detail);
        given(productRepository.findAllById(anySet())).willReturn(List.of());
        assertThatThrownBy(() -> stockService.validateAndReduceStockBatch(details))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("restoreStockBatch - should restore stock")
    void restoreStockBatch_ShouldRestoreStock() {
        Long productId = 1L;
        int initialStock = 100;
        int quantity = 5;
        Product product = TestFixtures.productWithIdAndStock(productId, initialStock);
        SaleDetail detail = TestFixtures.saleDetailWithProductAndQuantity(product, quantity);
        List<SaleDetail> details = List.of(detail);
        given(productRepository.findAllById(anySet())).willReturn(List.of(product));
        given(productRepository.saveAll(anyList())).willReturn(List.of(product));
        stockService.restoreStockBatch(details);
        assertThat(product.getQuantity()).isEqualTo(initialStock + quantity);
        then(productRepository).should().findAllById(Set.of(productId));
        then(productRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("restoreStockBatch - should handle empty list")
    void restoreStockBatch_WithEmptyList_ShouldDoNothing() {
        stockService.restoreStockBatch(List.of());
        then(productRepository).should(never()).findAllById(any());
        then(productRepository).should(never()).saveAll(any());
    }

    @Test
    @DisplayName("restoreStockBatch - should aggregate quantities for same product")
    void restoreStockBatch_WithMultipleDetailsSameProduct_ShouldAggregate() {
        Long productId = 1L;
        int initialStock = 100;
        Product product = TestFixtures.productWithIdAndStock(productId, initialStock);
        SaleDetail detail1 = TestFixtures.saleDetailWithProductAndQuantity(product, 5);
        SaleDetail detail2 = TestFixtures.saleDetailWithProductAndQuantity(product, 10);
        List<SaleDetail> details = List.of(detail1, detail2);
        given(productRepository.findAllById(anySet())).willReturn(List.of(product));
        given(productRepository.saveAll(anyList())).willReturn(List.of(product));
        stockService.restoreStockBatch(details);
        assertThat(product.getQuantity()).isEqualTo(initialStock + 15);
    }
}