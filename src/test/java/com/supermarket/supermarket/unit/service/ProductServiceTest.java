package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.dto.product.ProductRequest;
import com.supermarket.supermarket.dto.product.ProductResponse;
import com.supermarket.supermarket.exception.DuplicateResourceException;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.fixtures.TestFixtures;
import com.supermarket.supermarket.mapper.ProductMapper;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    @DisplayName("GET ALL - should return paginated list")
    void getAll_ShouldReturnPaginatedList() {
        Product product = TestFixtures.defaultProduct();
        ProductResponse response = TestFixtures.productResponse();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product));

        given(productRepository.findAll((Specification<Product>) any(), eq(pageable))).willReturn(page);
        given(productMapper.toResponse(product)).willReturn(response);

        Page<ProductResponse> result = productService.getAll(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("GET ALL DROPDOWN - should return simple list")
    void getAllForDropdown_ShouldReturnList() {
        Product product = TestFixtures.defaultProduct();
        ProductResponse response = TestFixtures.productResponse();

        given(productRepository.findAll()).willReturn(List.of(product));
        given(productMapper.toResponseList(List.of(product))).willReturn(List.of(response));

        List<ProductResponse> result = productService.getAllForDropdown();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("GET BY ID - should return product")
    void getById_ShouldReturnProduct() {
        Long id = 1L;
        Product product = TestFixtures.defaultProduct();
        ProductResponse response = TestFixtures.productResponse();

        given(productRepository.findById(id)).willReturn(Optional.of(product));
        given(productMapper.toResponse(product)).willReturn(response);

        ProductResponse result = productService.getById(id);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Premium Rice");
    }

    @Test
    @DisplayName("GET BY ID - should throw exception when not found")
    void getById_WhenNotFound_ShouldThrowException() {
        given(productRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("CREATE - should save product when name is unique")
    void create_WhenNameUnique_ShouldSave() {
        ProductRequest request = TestFixtures.validProductRequest();
        Product entity = TestFixtures.defaultProduct();
        ProductResponse response = TestFixtures.productResponse();

        given(productRepository.existsByName(request.getName())).willReturn(false);
        given(productMapper.toEntity(request)).willReturn(entity);
        given(productRepository.save(entity)).willReturn(entity);
        given(productMapper.toResponse(entity)).willReturn(response);

        ProductResponse result = productService.create(request);

        assertThat(result).isNotNull();
        then(productRepository).should().save(entity);
    }

    @Test
    @DisplayName("CREATE - should throw exception when name exists")
    void create_WhenNameExists_ShouldThrowException() {
        ProductRequest request = TestFixtures.validProductRequest();

        given(productRepository.existsByName(request.getName())).willReturn(true);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        then(productRepository).should(never()).save(any(Product.class));
    }

    @Test
    @DisplayName("UPDATE - should update product")
    void update_ShouldUpdateProduct() {
        Long id = 1L;
        ProductRequest request = TestFixtures.validProductRequest();
        Product existingProduct = TestFixtures.defaultProduct();
        ProductResponse response = TestFixtures.productResponse();

        given(productRepository.findById(id)).willReturn(Optional.of(existingProduct));
        given(productRepository.existsByName(request.getName())).willReturn(false);
        given(productRepository.save(existingProduct)).willReturn(existingProduct);
        given(productMapper.toResponse(existingProduct)).willReturn(response);

        ProductResponse result = productService.update(id, request);

        assertThat(result).isNotNull();
        then(productMapper).should().updateEntity(request, existingProduct);
    }

    @Test
    @DisplayName("UPDATE - should throw exception when duplicate name")
    void update_WithDuplicateName_ShouldThrowException() {
        Long id = 1L;
        ProductRequest request = TestFixtures.validProductRequest();
        Product existingProduct = TestFixtures.defaultProduct();

        given(productRepository.findById(id)).willReturn(Optional.of(existingProduct));
        given(productRepository.existsByName(request.getName())).willReturn(true);

        assertThatThrownBy(() -> productService.update(id, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("DELETE - should delete product when no sales associated")
    void delete_ShouldDeleteProduct() {
        Long id = 1L;
        Product product = TestFixtures.defaultProduct();

        given(productRepository.findById(id)).willReturn(Optional.of(product));
        given(saleRepository.existsByDetailsProductId(id)).willReturn(false);

        productService.delete(id);

        then(productRepository).should().delete(product);
    }

    @Test
    @DisplayName("DELETE - should throw exception when product has associated sales")
    void delete_WhenProductHasSales_ShouldThrowException() {
        Long id = 1L;
        Product product = TestFixtures.defaultProduct();

        given(productRepository.findById(id)).willReturn(Optional.of(product));
        given(saleRepository.existsByDetailsProductId(id)).willReturn(true);

        assertThatThrownBy(() -> productService.delete(id))
                .isInstanceOf(InvalidOperationException.class);

        then(productRepository).should(never()).delete((Product) any());
    }

    @Test
    @DisplayName("DELETE - should throw exception when product not found")
    void delete_WhenNotFound_ShouldThrowException() {
        Long id = 999L;
        given(productRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        then(productRepository).should(never()).delete((Product) any());
    }
}