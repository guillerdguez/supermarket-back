package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.dto.sale.CancelSaleRequest;
import com.supermarket.supermarket.dto.sale.SaleRequest;
import com.supermarket.supermarket.dto.sale.SaleResponse;
import com.supermarket.supermarket.exception.InsufficientPermissionsException;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.InvalidSaleStateException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.SaleMapper;
import com.supermarket.supermarket.model.branch.Branch;
import com.supermarket.supermarket.model.cashregister.CashRegister;
import com.supermarket.supermarket.model.product.Product;
import com.supermarket.supermarket.model.sale.Sale;
import com.supermarket.supermarket.model.sale.SaleStatus;
import com.supermarket.supermarket.model.user.User;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.security.SecurityUtils;
import com.supermarket.supermarket.service.business.CashRegisterService;
import com.supermarket.supermarket.service.business.InventoryService;
import com.supermarket.supermarket.service.business.NotificationEventService;
import com.supermarket.supermarket.service.business.impl.SaleServiceImpl;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.supermarket.supermarket.fixtures.branch.BranchFixtures.defaultBranch;
import static com.supermarket.supermarket.fixtures.cashregister.CashRegisterFixtures.openRegister;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

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
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private CashRegisterService cashRegisterService;
    @Mock
    private NotificationEventService notificationEventService;

    @InjectMocks
    private SaleServiceImpl saleService;

    private User mockUser;
    private CashRegister mockOpenRegister;

    @BeforeEach
    void setUp() {
        mockUser = defaultCashier();
        mockOpenRegister = openRegister();
    }

    @Test
    @DisplayName("CREATE - should create sale and reduce stock")
    void create_WithSufficientStock_ShouldCreate() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);
        given(cashRegisterService.getRegisterEntityByBranch(anyLong())).willReturn(mockOpenRegister);

        SaleRequest request = validSaleRequest();
        Branch branch = defaultBranch();
        Product product = defaultProduct();
        Sale sale = saleWithDetails();
        SaleResponse response = saleResponse();

        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));
        given(saleMapper.toEntity(request)).willReturn(sale);

        Set<Long> productIds = Set.of(1L);
        given(productRepository.findAllById(productIds)).willReturn(List.of(product));

        willDoNothing().given(inventoryService)
                .validateAndReduceStockBatch(request.getBranchId(), request.getDetails());

        given(saleRepository.save(any(Sale.class))).willReturn(sale);
        given(saleMapper.toResponse(sale)).willReturn(response);

        SaleResponse result = saleService.create(request);

        assertThat(result).isNotNull();
        then(inventoryService).should().validateAndReduceStockBatch(request.getBranchId(), request.getDetails());
        then(productRepository).should().findAllById(productIds);
        then(saleRepository).should().save(sale);
    }

    @Test
    @DisplayName("CREATE - should throw exception when stock is insufficient")
    void create_WithInsufficientStock_ShouldThrowException() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);
        given(cashRegisterService.getRegisterEntityByBranch(anyLong())).willReturn(mockOpenRegister);

        SaleRequest request = validSaleRequest();
        Branch branch = defaultBranch();
        Sale sale = saleWithDetails();
        Product product = defaultProduct();

        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));
        given(saleMapper.toEntity(request)).willReturn(sale);

        Set<Long> productIds = Set.of(1L);
        given(productRepository.findAllById(productIds)).willReturn(List.of(product));

        willThrow(new InsufficientStockException("Insufficient stock"))
                .given(inventoryService).validateAndReduceStockBatch(request.getBranchId(), request.getDetails());

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(InsufficientStockException.class);
        then(saleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CREATE - should throw exception when branch not found")
    void create_WhenBranchNotFound_ShouldThrowException() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);

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
        given(securityUtils.getCurrentUser()).willReturn(mockUser);
        given(cashRegisterService.getRegisterEntityByBranch(anyLong())).willReturn(mockOpenRegister);

        SaleRequest request = validSaleRequest();
        request.getDetails().get(0).setProductId(999L);
        Branch branch = defaultBranch();
        Sale sale = saleWithDetails();

        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));
        given(saleMapper.toEntity(request)).willReturn(sale);

        Set<Long> productIds = Set.of(999L);
        given(productRepository.findAllById(productIds)).willReturn(List.of());

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Products not found with IDs: [999]");
        then(saleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CREATE - should throw exception when no open cash register exists")
    void create_WhenNoOpenRegister_ShouldThrowException() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);
        given(cashRegisterService.getRegisterEntityByBranch(anyLong()))
                .willThrow(new ResourceNotFoundException("No open register found for branch"));

        SaleRequest request = validSaleRequest();
        Branch branch = defaultBranch();
        given(branchRepository.findById(1L)).willReturn(Optional.of(branch));

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No open register found for branch");
        then(inventoryService).shouldHaveNoInteractions();
        then(saleRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("CANCEL - should cancel sale and restore stock")
    void cancel_ShouldCancelSaleAndRestoreStock() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);

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
        given(securityUtils.getCurrentUser()).willReturn(mockUser);

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