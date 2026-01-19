package com.supermercado.supermercado.unit.service;

import com.supermercado.supermercado.dto.ventaDto.VentaRequest;
import com.supermercado.supermercado.dto.ventaDto.VentaResponse;
import com.supermercado.supermercado.exception.InsufficientStockException;
import com.supermercado.supermercado.exception.InvalidSaleStateException;
import com.supermercado.supermercado.exception.ResourceNotFoundException;
import com.supermercado.supermercado.fixtures.TestFixtures;
import com.supermercado.supermercado.mapper.VentaMapper;
import com.supermercado.supermercado.model.Estado;
import com.supermercado.supermercado.model.Producto;
import com.supermercado.supermercado.model.Sucursal;
import com.supermercado.supermercado.model.Venta;
import com.supermercado.supermercado.repository.ProductoRepository;
import com.supermercado.supermercado.repository.SucursalRepository;
import com.supermercado.supermercado.repository.VentaRepository;
import com.supermercado.supermercado.service.impl.VentaServiceImpl;
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
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private VentaRepository saleRepository;
    @Mock
    private ProductoRepository productRepository;
    @Mock
    private SucursalRepository branchRepository;
    @Mock
    private VentaMapper saleMapper;
    @InjectMocks
    private VentaServiceImpl saleService;

    @Test
    @DisplayName("CREATE - should create sale and reduce stock")
    void create_WithSufficientStock_ShouldCreate() {
        VentaRequest request = TestFixtures.validSaleRequest();
        Producto product = TestFixtures.defaultProduct();
        Sucursal branch = TestFixtures.defaultBranch();
        Venta sale = TestFixtures.saleWithDetails();
        VentaResponse response = TestFixtures.saleResponse();

        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(saleMapper.toEntity(request)).willReturn(sale);
        given(saleRepository.save(any(Venta.class))).willReturn(sale);
        given(saleMapper.toResponse(any(Venta.class))).willReturn(response);

        VentaResponse result = saleService.create(request);

        assertThat(result).isNotNull();
        assertThat(product.getCantidad()).isEqualTo(95);
        then(saleRepository).should().save(any(Venta.class));
    }

    @Test
    @DisplayName("CREATE - should throw exception when stock is insufficient")
    void create_WithInsufficientStock_ShouldThrowException() {
        VentaRequest request = TestFixtures.validSaleRequest();
        Producto lowStockProduct = TestFixtures.productWithIdAndStock(1L, 2);
        Sucursal branch = TestFixtures.defaultBranch();
        Venta sale = TestFixtures.saleWithDetails();

        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));
        given(productRepository.findById(1L)).willReturn(Optional.of(lowStockProduct));
        given(saleMapper.toEntity(request)).willReturn(sale);

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(InsufficientStockException.class);

        then(saleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CREATE - should throw exception when branch not found")
    void create_WhenBranchNotFound_ShouldThrowException() {
        VentaRequest request = TestFixtures.validSaleRequest();
        request.setIdSucursal(999L);

        Venta sale = TestFixtures.saleWithDetails();

        given(branchRepository.findById(999L)).willReturn(Optional.empty());

        given(saleMapper.toEntity(request)).willReturn(sale);

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        then(saleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CREATE - should throw exception when product not found")
    void create_WhenProductNotFound_ShouldThrowException() {
        VentaRequest request = TestFixtures.validSaleRequest();

        request.getDetalles().get(0).setProductoId(999L);

        Sucursal branch = TestFixtures.defaultBranch();
        Venta sale = TestFixtures.saleWithDetails();

        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));

        given(productRepository.findById(999L)).willReturn(Optional.empty());

        given(saleMapper.toEntity(request)).willReturn(sale);

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        then(saleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("UPDATE - should update sale")
    void update_ShouldUpdateSale() {

        Long id = 100L;
        VentaRequest request = TestFixtures.validSaleRequest();
        Venta existingSale = TestFixtures.saleWithDetails();
        Producto product = TestFixtures.defaultProduct();
        VentaResponse response = TestFixtures.saleResponse();

        given(saleRepository.findById(id)).willReturn(Optional.of(existingSale));
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        given(saleRepository.save(existingSale)).willReturn(existingSale);
        given(saleMapper.toResponse(existingSale)).willReturn(response);

        VentaResponse result = saleService.update(id, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);

        then(saleRepository).should().findById(id);
        then(productRepository).should().findById(1L);
        then(saleRepository).should().save(existingSale);
    }

    @Test
    @DisplayName("UPDATE - should throw exception when sale is cancelled")
    void update_WhenSaleCancelled_ShouldThrowException() {
        Long id = 100L;
        VentaRequest request = TestFixtures.validSaleRequest();
        Venta cancelledSale = TestFixtures.saleWithDetails();
        cancelledSale.setEstado(Estado.ANULADA);

        given(saleRepository.findById(id)).willReturn(Optional.of(cancelledSale));

        assertThatThrownBy(() -> saleService.update(id, request))
                .isInstanceOf(InvalidSaleStateException.class);

        then(saleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("GET BY ID - should return sale")
    void getById_ShouldReturnSale() {
        Long id = 100L;
        Venta sale = TestFixtures.saleWithDetails();
        VentaResponse response = TestFixtures.saleResponse();

        given(saleRepository.findById(id)).willReturn(Optional.of(sale));
        given(saleMapper.toResponse(sale)).willReturn(response);

        VentaResponse result = saleService.getById(id);

        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(12.50);
    }

    @Test
    @DisplayName("GET BY ID - should throw exception when not found")
    void getById_WhenNotFound_ShouldThrowException() {
        Long id = 999L;
        given(saleRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("DELETE - should delete sale")
    void delete_ShouldDeleteSale() {
        Long id = 100L;
        Venta sale = TestFixtures.saleWithDetails();

        given(saleRepository.findById(id)).willReturn(Optional.of(sale));

        saleService.delete(id);

        then(saleRepository).should().delete(sale);
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