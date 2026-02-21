package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.dto.sale.CancelSaleRequest;
import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.exception.InsufficientPermissionsException;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.InvalidSaleStateException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.SaleMapper;
import com.supermarket.supermarket.model.Branch;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.Sale;
import com.supermarket.supermarket.model.SaleStatus;
import com.supermarket.supermarket.model.User;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.service.business.InventoryService;
import com.supermarket.supermarket.service.business.impl.SaleServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static com.supermarket.supermarket.fixtures.branch.BranchFixtures.defaultBranch;
import static com.supermarket.supermarket.fixtures.product.ProductFixtures.defaultProduct;
import static com.supermarket.supermarket.fixtures.sale.SaleFixtures.cancelledSaleResponse;
import static com.supermarket.supermarket.fixtures.sale.SaleFixtures.saleResponse;
import static com.supermarket.supermarket.fixtures.sale.SaleFixtures.saleWithDetails;
import static com.supermarket.supermarket.fixtures.sale.SaleFixtures.validCancelRequest;
import static com.supermarket.supermarket.fixtures.sale.SaleFixtures.validSaleRequest;
import static com.supermarket.supermarket.fixtures.user.UserFixtures.defaultCashier;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {
    @Mock
    private SaleRepository saleRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private SaleMapper saleMapper;
    @InjectMocks
    private SaleServiceImpl saleService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = defaultCashier();
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("CREATE - should create sale and reduce stock")
    void create_WithSufficientStock_ShouldCreate() {
        SaleRequest request = validSaleRequest();
        Branch branch = defaultBranch();
        Product product = defaultProduct();
        Sale sale = saleWithDetails();
        SaleResponse response = saleResponse();

        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));
        given(saleMapper.toEntity(request)).willReturn(sale);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(saleRepository.save(any(Sale.class))).willReturn(sale);
        given(saleMapper.toResponse(sale)).willReturn(response);

        SaleResponse result = saleService.create(request);

        assertThat(result).isNotNull();
        then(inventoryService).should().validateAndReduceStockBatch(request.getBranchId(), request.getDetails());
        then(productRepository).should().findById(1L);
        then(saleRepository).should().save(sale);
    }

    @Test
    @DisplayName("CREATE - should throw exception when stock is insufficient")
    void create_WithInsufficientStock_ShouldThrowException() {
        SaleRequest request = validSaleRequest();
        Branch branch = defaultBranch();
        Sale sale = saleWithDetails();

        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));
        given(saleMapper.toEntity(request)).willReturn(sale);
        willThrow(new InsufficientStockException("Insufficient stock"))
                .given(inventoryService).validateAndReduceStockBatch(request.getBranchId(), request.getDetails());

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(InsufficientStockException.class);
        then(productRepository).shouldHaveNoInteractions();
        then(saleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CREATE - should throw exception when branch not found")
    void create_WhenBranchNotFound_ShouldThrowException() {
        SaleRequest request = validSaleRequest();
        request.setBranchId(999L);

        given(branchRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        then(inventoryService).shouldHaveNoInteractions();
        then(productRepository).shouldHaveNoInteractions();
        then(saleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CREATE - should throw exception when product not found")
    void create_WhenProductNotFound_ShouldThrowException() {
        SaleRequest request = validSaleRequest();
        request.getDetails().get(0).setProductId(999L);
        Branch branch = defaultBranch();
        Sale sale = saleWithDetails();

        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));
        given(saleMapper.toEntity(request)).willReturn(sale);
        given(productRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
        then(saleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CANCEL - should cancel sale and restore stock")
    void cancel_ShouldCancelSaleAndRestoreStock() {
        Long id = 100L;
        CancelSaleRequest request = validCancelRequest();
        Sale sale = saleWithDetails();
        SaleResponse response = cancelledSaleResponse();

        given(saleRepository.findWithDetailsById(id)).willReturn(Optional.of(sale));
        given(saleRepository.save(sale)).willReturn(sale);
        given(saleMapper.toResponse(sale)).willReturn(response);

        SaleResponse result = saleService.cancel(id, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        then(inventoryService).should().restoreStockBatch(sale.getBranch().getId(), sale.getDetails());
        then(saleRepository).should().save(sale);
        assertThat(sale.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        assertThat(sale.getCancellationReason()).isEqualTo(request.getReason());
        assertThat(sale.getCancelledBy()).isEqualTo(mockUser);
        assertThat(sale.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("CANCEL - should throw exception when sale is already cancelled")
    void cancel_WhenAlreadyCancelled_ShouldThrowException() {
        Long id = 100L;
        CancelSaleRequest request = validCancelRequest();
        Sale sale = saleWithDetails();
        sale.setStatus(SaleStatus.CANCELLED);

        given(saleRepository.findWithDetailsById(id)).willReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleService.cancel(id, request))
                .isInstanceOf(InvalidSaleStateException.class)
                .hasMessageContaining("already cancelled");
        then(inventoryService).shouldHaveNoInteractions();
        then(saleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CANCEL - should throw exception when sale not found")
    void cancel_WhenNotFound_ShouldThrowException() {
        given(saleRepository.findWithDetailsById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.cancel(999L, validCancelRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        then(inventoryService).shouldHaveNoInteractions();
        then(saleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("GET BY ID - should return sale")
    void getById_ShouldReturnSale() {
        Long id = 100L;
        Sale sale = saleWithDetails();
        SaleResponse response = saleResponse();

        given(saleRepository.findWithDetailsById(id)).willReturn(Optional.of(sale));
        given(saleMapper.toResponse(sale)).willReturn(response);

        SaleResponse result = saleService.getById(id);

        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(saleResponse().getTotal());
    }

    @Test
    @DisplayName("GET BY ID - should throw exception when not found")
    void getById_WhenNotFound_ShouldThrowException() {
        given(saleRepository.findWithDetailsById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("DELETE - should delete sale and restore stock")
    void delete_ShouldDeleteSale() {
        Long id = 100L;
        Sale sale = saleWithDetails();
        sale.setStatus(SaleStatus.REGISTERED);

        given(saleRepository.findById(id)).willReturn(Optional.of(sale));

        saleService.delete(id);

        then(inventoryService).should().restoreStockBatch(sale.getBranch().getId(), sale.getDetails());
        then(saleRepository).should().delete(sale);
    }

    @Test
    @DisplayName("DELETE - should not restore stock if sale is cancelled")
    void delete_WhenSaleCancelled_ShouldNotRestoreStock() {
        Long id = 100L;
        Sale sale = saleWithDetails();
        sale.setStatus(SaleStatus.CANCELLED);

        given(saleRepository.findById(id)).willReturn(Optional.of(sale));

        saleService.delete(id);

        then(inventoryService).shouldHaveNoInteractions();
        then(saleRepository).should().delete(sale);
    }

    @Test
    @DisplayName("DELETE - should throw exception when sale not found")
    void delete_WhenNotFound_ShouldThrowException() {
        given(saleRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        then(inventoryService).shouldHaveNoInteractions();
        then(saleRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("GET SALES BY CASHIER - should return paginated cashier sales")
    void getSalesByCashier_ShouldReturnPage() {
        Sale sale = saleWithDetails();
        SaleResponse response = saleResponse();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Sale> salePage = new PageImpl<>(List.of(sale));

        given(saleRepository.findByCreatedById(1L, pageable)).willReturn(salePage);
        given(saleMapper.toResponse(sale)).willReturn(response);

        Page<SaleResponse> result = saleService.getSalesByCashier(1L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCreatedById()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GET SALE BY ID AND CASHIER - should return sale when owner")
    void getSaleByIdAndCashier_WhenOwner_ShouldReturnSale() {
        Sale sale = saleWithDetails();
        SaleResponse response = saleResponse();

        given(saleRepository.findWithDetailsById(100L)).willReturn(Optional.of(sale));
        given(saleMapper.toResponse(sale)).willReturn(response);

        SaleResponse result = saleService.getSaleByIdAndCashier(100L, 1L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("GET SALE BY ID AND CASHIER - should throw exception when not owner")
    void getSaleByIdAndCashier_WhenNotOwner_ShouldThrowException() {
        Sale sale = saleWithDetails();

        given(saleRepository.findWithDetailsById(100L)).willReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleService.getSaleByIdAndCashier(100L, 99L))
                .isInstanceOf(InsufficientPermissionsException.class);
    }

    @Test
    @DisplayName("GET SALE BY ID AND CASHIER - should throw exception when createdBy is null")
    void getSaleByIdAndCashier_WhenCreatedByNull_ShouldThrowException() {
        Sale sale = saleWithDetails();
        sale.setCreatedBy(null);

        given(saleRepository.findWithDetailsById(100L)).willReturn(Optional.of(sale));

        assertThatThrownBy(() -> saleService.getSaleByIdAndCashier(100L, 1L))
                .isInstanceOf(InsufficientPermissionsException.class);
    }
}