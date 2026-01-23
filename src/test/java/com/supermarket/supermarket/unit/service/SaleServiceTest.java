package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.InvalidSaleStateException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.fixtures.TestFixtures;
import com.supermarket.supermarket.mapper.SaleMapper;
import com.supermarket.supermarket.model.Branch;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.Sale;
import com.supermarket.supermarket.model.SaleStatus;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.service.ProductService;
import com.supermarket.supermarket.service.impl.SaleServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductService productService;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private SaleMapper saleMapper;

    @InjectMocks
    private SaleServiceImpl saleService;

    @Test
    @DisplayName("CREATE - should create sale and reduce stock")
    void create_WithSufficientStock_ShouldCreate() {
        SaleRequest request = TestFixtures.validSaleRequest();
        Product product = TestFixtures.defaultProduct();
        Branch branch = TestFixtures.defaultBranch();
        Sale sale = TestFixtures.saleWithDetails();
        SaleResponse response = TestFixtures.saleResponse();

        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));
        given(saleMapper.toEntity(request)).willReturn(sale);
        given(saleRepository.save(any(Sale.class))).willReturn(sale);
        given(saleMapper.toResponse(any(Sale.class))).willReturn(response);
        given(productService.reduceStock(1L, 5)).willReturn(product);

        SaleResponse result = saleService.create(request);

        assertThat(result).isNotNull();
        then(saleRepository).should().save(any(Sale.class));
        then(productService).should().reduceStock(1L, 5);
    }

    @Test
    @DisplayName("CREATE - should throw exception when stock is insufficient")
    void create_WithInsufficientStock_ShouldThrowException() {
        SaleRequest request = TestFixtures.validSaleRequest();
        Branch branch = TestFixtures.defaultBranch();
        Sale sale = TestFixtures.saleWithDetails();

        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));
        given(saleMapper.toEntity(request)).willReturn(sale);
        doThrow(new InsufficientStockException("Insufficient stock"))
                .when(productService).reduceStock(1L, 5);

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(InsufficientStockException.class);

        then(saleRepository).should(never()).save(any());
        then(productService).should().reduceStock(1L, 5);
    }

    @Test
    @DisplayName("CREATE - should throw exception when branch not found")
    void create_WhenBranchNotFound_ShouldThrowException() {
        SaleRequest request = TestFixtures.validSaleRequest();
        request.setBranchId(999L);
        Sale sale = TestFixtures.saleWithDetails();

        given(branchRepository.findById(999L)).willReturn(Optional.empty());
        given(saleMapper.toEntity(request)).willReturn(sale);

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        then(saleRepository).should(never()).save(any());
        then(productService).should(never()).reduceStock(any(), any());
    }

    @Test
    @DisplayName("CREATE - should throw exception when product not found")
    void create_WhenProductNotFound_ShouldThrowException() {
        SaleRequest request = TestFixtures.validSaleRequest();
        request.getDetails().get(0).setProductId(999L);
        Branch branch = TestFixtures.defaultBranch();
        Sale sale = TestFixtures.saleWithDetails();

        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));
        given(saleMapper.toEntity(request)).willReturn(sale);
        doThrow(new ResourceNotFoundException("Product not found"))
                .when(productService).reduceStock(999L, 5);

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        then(saleRepository).should(never()).save(any());
        then(productService).should().reduceStock(999L, 5);
    }

    @Test
    @DisplayName("UPDATE - should update sale")
    void update_ShouldUpdateSale() {
        Long id = 100L;
        SaleRequest request = TestFixtures.validSaleRequest();
        Sale existingSale = TestFixtures.saleWithDetails();
        Product product = TestFixtures.defaultProduct();
        SaleResponse response = TestFixtures.saleResponse();

        given(saleRepository.findById(id)).willReturn(Optional.of(existingSale));
        given(saleRepository.save(existingSale)).willReturn(existingSale);
        given(saleMapper.toResponse(existingSale)).willReturn(response);
        given(productService.reduceStock(1L, 5)).willReturn(product);

        SaleResponse result = saleService.update(id, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);

        then(saleRepository).should().findById(id);
        then(productService).should().increaseStock(1L, 5);
        then(productService).should().reduceStock(1L, 5);
        then(saleRepository).should().save(existingSale);
    }

    @Test
    @DisplayName("UPDATE - should throw exception when sale is cancelled")
    void update_WhenSaleCancelled_ShouldThrowException() {
        Long id = 100L;
        SaleRequest request = TestFixtures.validSaleRequest();
        Sale cancelledSale = TestFixtures.saleWithDetails();
        cancelledSale.setStatus(SaleStatus.CANCELLED);

        given(saleRepository.findById(id)).willReturn(Optional.of(cancelledSale));

        assertThatThrownBy(() -> saleService.update(id, request))
                .isInstanceOf(InvalidSaleStateException.class);

        then(saleRepository).should(never()).save(any());
        then(productService).should(never()).increaseStock(any(), any());
        then(productService).should(never()).reduceStock(any(), any());
    }

    @Test
    @DisplayName("GET BY ID - should return sale")
    void getById_ShouldReturnSale() {
        Long id = 100L;
        Sale sale = TestFixtures.saleWithDetails();
        SaleResponse response = TestFixtures.saleResponse();

        given(saleRepository.findWithDetailsById(id)).willReturn(Optional.of(sale));
        given(saleMapper.toResponse(sale)).willReturn(response);

        SaleResponse result = saleService.getById(id);

        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(12.50);
    }

    @Test
    @DisplayName("GET BY ID - should throw exception when not found")
    void getById_WhenNotFound_ShouldThrowException() {
        Long id = 999L;

        given(saleRepository.findWithDetailsById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("DELETE - should delete sale and restore stock")
    void delete_ShouldDeleteSale() {
        Long id = 100L;
        Sale sale = TestFixtures.saleWithDetails();

        given(saleRepository.findById(id)).willReturn(Optional.of(sale));

        saleService.delete(id);

        then(saleRepository).should().delete(sale);
        then(productService).should().increaseStock(1L, 5);
    }

    @Test
    @DisplayName("DELETE - should throw exception when sale not found")
    void delete_WhenNotFound_ShouldThrowException() {
        Long id = 999L;

        given(saleRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        then(saleRepository).should(never()).delete(any());
    }
}