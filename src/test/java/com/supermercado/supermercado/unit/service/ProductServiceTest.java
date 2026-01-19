package com.supermercado.supermercado.unit.service;

import com.supermercado.supermercado.dto.productoDto.ProductoRequest;
import com.supermercado.supermercado.dto.productoDto.ProductoResponse;
import com.supermercado.supermercado.exception.DuplicateResourceException;
import com.supermercado.supermercado.exception.ResourceNotFoundException;
import com.supermercado.supermercado.fixtures.TestFixtures;
import com.supermercado.supermercado.mapper.ProductoMapper;
import com.supermercado.supermercado.model.Producto;
import com.supermercado.supermercado.repository.ProductoRepository;
import com.supermercado.supermercado.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductoRepository productRepository;
    @Mock
    private ProductoMapper productMapper;
    @InjectMocks
    private ProductoServiceImpl productService;

    @Test
    @DisplayName("GET ALL - should return list")
    void getAll_ShouldReturnList() {
        Producto product = TestFixtures.defaultProduct();
        ProductoResponse response = TestFixtures.productResponse();

        given(productRepository.findAll()).willReturn(List.of(product));
        given(productMapper.toResponseList(List.of(product))).willReturn(List.of(response));

        List<ProductoResponse> result = productService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Premium Rice");
    }

    @Test
    @DisplayName("GET BY ID - should return product")
    void getById_ShouldReturnProduct() {
        Long id = 1L;
        Producto product = TestFixtures.defaultProduct();
        ProductoResponse response = TestFixtures.productResponse();

        given(productRepository.findById(id)).willReturn(Optional.of(product));
        given(productMapper.toResponse(product)).willReturn(response);

        ProductoResponse result = productService.getById(id);

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Premium Rice");
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
        ProductoRequest request = TestFixtures.validProductRequest();
        Producto entity = TestFixtures.defaultProduct();
        ProductoResponse response = TestFixtures.productResponse();

        given(productRepository.existsByNombre(request.getNombre())).willReturn(false);
        given(productMapper.toEntity(request)).willReturn(entity);
        given(productRepository.save(entity)).willReturn(entity);
        given(productMapper.toResponse(entity)).willReturn(response);

        ProductoResponse result = productService.create(request);

        assertThat(result).isNotNull();
        then(productRepository).should().save(entity);
    }

    @Test
    @DisplayName("CREATE - should throw exception when name exists")
    void create_WhenNameExists_ShouldThrowException() {
        ProductoRequest request = TestFixtures.validProductRequest();
        given(productRepository.existsByNombre(request.getNombre())).willReturn(true);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        then(productRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("UPDATE - should update product")
    void update_ShouldUpdateProduct() {
        Long id = 1L;
        ProductoRequest request = TestFixtures.validProductRequest();
        Producto existingProduct = TestFixtures.defaultProduct();
        ProductoResponse response = TestFixtures.productResponse();

        given(productRepository.findById(id)).willReturn(Optional.of(existingProduct));
        given(productRepository.existsByNombre(request.getNombre())).willReturn(false);
        given(productRepository.save(existingProduct)).willReturn(existingProduct);
        given(productMapper.toResponse(existingProduct)).willReturn(response);

        ProductoResponse result = productService.update(id, request);

        assertThat(result).isNotNull();
        then(productMapper).should().updateEntity(request, existingProduct);
    }

    @Test
    @DisplayName("UPDATE - should update stock correctly")
    void update_ShouldUpdateStock() {
        Long id = 1L;
        ProductoRequest request = ProductoRequest.builder()
                .nombre("Updated Product")
                .categoria("Updated Category")
                .precio(15.0)
                .cantidad(200)
                .build();

        Producto existingProduct = TestFixtures.defaultProduct();
        ProductoResponse response = ProductoResponse.builder()
                .id(1L)
                .nombre("Updated Product")
                .categoria("Updated Category")
                .precio(15.0)
                .cantidad(200)
                .build();

        given(productRepository.findById(id)).willReturn(Optional.of(existingProduct));
        given(productRepository.existsByNombre(request.getNombre())).willReturn(false);
        given(productRepository.save(existingProduct)).willReturn(existingProduct);
        given(productMapper.toResponse(existingProduct)).willReturn(response);

        ProductoResponse result = productService.update(id, request);

        assertThat(result).isNotNull();
        assertThat(result.getCantidad()).isEqualTo(200);
        then(productMapper).should().updateEntity(request, existingProduct);
    }

    @Test
    @DisplayName("UPDATE - should throw exception when duplicate name")
    void update_WithDuplicateName_ShouldThrowException() {
        Long id = 1L;
        ProductoRequest request = TestFixtures.validProductRequest();
        Producto existingProduct = TestFixtures.defaultProduct();

        given(productRepository.findById(id)).willReturn(Optional.of(existingProduct));
        given(productRepository.existsByNombre(request.getNombre())).willReturn(true);

        assertThatThrownBy(() -> productService.update(id, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("DELETE - should delete product")
    void delete_ShouldDeleteProduct() {
        Long id = 1L;
        Producto product = TestFixtures.defaultProduct();

        given(productRepository.findById(id)).willReturn(Optional.of(product));

        productService.delete(id);

        then(productRepository).should().delete(product);
    }

    @Test
    @DisplayName("DELETE - should throw exception when product not found")
    void delete_WhenNotFound_ShouldThrowException() {
        Long id = 999L;
        given(productRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        then(productRepository).should(never()).delete(any());
    }
}