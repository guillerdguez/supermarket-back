package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.dto.inventory.BranchInventoryResponse;
import com.supermarket.supermarket.dto.inventory.LowStockAlertResponse;
import com.supermarket.supermarket.dto.inventory.StockAdjustmentRequest;
import com.supermarket.supermarket.dto.inventory.StockUpdateRequest;
import com.supermarket.supermarket.dto.inventory.TotalStockResponse;
import com.supermarket.supermarket.dto.saleDetail.SaleDetailRequest;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.BranchInventoryMapper;
import com.supermarket.supermarket.model.branch.Branch;
import com.supermarket.supermarket.model.branch.BranchInventory;
import com.supermarket.supermarket.model.product.Product;
import com.supermarket.supermarket.model.sale.SaleDetail;
import com.supermarket.supermarket.repository.BranchInventoryRepository;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.service.business.NotificationEventService;
import com.supermarket.supermarket.service.business.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.supermarket.supermarket.fixtures.branch.BranchFixtures.defaultBranch;
import static com.supermarket.supermarket.fixtures.product.ProductFixtures.defaultProduct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @Mock
    private BranchInventoryRepository branchInventoryRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private BranchInventoryMapper branchInventoryMapper;
    @Mock
    private NotificationEventService notificationEventService;

    @InjectMocks
    private InventoryServiceImpl inventoryService;
    private Branch branch;
    private Product product;
    private BranchInventory inventory;

    @BeforeEach
    void setUp() {
        branch = defaultBranch();
        product = defaultProduct();
        inventory = BranchInventory.builder()
                .id(100L)
                .branch(branch)
                .product(product)
                .stock(50)
                .minStock(5)
                .lastRestockDate(LocalDateTime.now())
                .version(0L)
                .build();
    }

    @Nested
    @DisplayName("Stock queries")
    class StockQueries {
        @Test
        @DisplayName("getStockInBranch - should return stock when inventory exists")
        void getStockInBranch_WhenExists_ShouldReturnStock() {
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                    .willReturn(Optional.of(inventory));
            Integer stock = inventoryService.getStockInBranch(1L, 1L);
            assertThat(stock).isEqualTo(50);
        }

        @Test
        @DisplayName("getStockInBranch - should return 0 when inventory not found")
        void getStockInBranch_WhenNotFound_ShouldReturnZero() {
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 99L))
                    .willReturn(Optional.empty());
            Integer stock = inventoryService.getStockInBranch(1L, 99L);
            assertThat(stock).isZero();
        }

        @Test
        @DisplayName("getLowStockInBranch - should return list of low stock items")
        void getLowStockInBranch_ShouldReturnList() {
            BranchInventory lowStockItem = BranchInventory.builder()
                    .branch(branch)
                    .product(product)
                    .stock(3)
                    .minStock(5)
                    .build();
            given(branchInventoryRepository.findLowStockByBranchId(1L))
                    .willReturn(List.of(lowStockItem));
            List<LowStockAlertResponse> result = inventoryService.getLowStockInBranch(1L);
            assertThat(result).hasSize(1);
            LowStockAlertResponse alert = result.get(0);
            assertThat(alert.getBranchId()).isEqualTo(1L);
            assertThat(alert.getProductId()).isEqualTo(1L);
            assertThat(alert.getCurrentStock()).isEqualTo(3);
            assertThat(alert.getMinStock()).isEqualTo(5);
        }

        @Test
        @DisplayName("getLowStockGlobal - should return list of low stock items globally")
        void getLowStockGlobal_ShouldReturnList() {
            BranchInventory lowStockItem = BranchInventory.builder()
                    .branch(branch)
                    .product(product)
                    .stock(2)
                    .minStock(5)
                    .build();
            given(branchInventoryRepository.findLowStockGlobal())
                    .willReturn(List.of(lowStockItem));
            List<LowStockAlertResponse> result = inventoryService.getLowStockGlobal();
            assertThat(result).hasSize(1);
            LowStockAlertResponse alert = result.get(0);
            assertThat(alert.getCurrentStock()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Single stock operations")
    class SingleStockOperations {
        @Test
        @DisplayName("validateAndReduceStock - should reduce stock when sufficient")
        void validateAndReduceStock_Success() {
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                    .willReturn(Optional.of(inventory));
            given(branchInventoryRepository.save(any(BranchInventory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            inventoryService.validateAndReduceStock(1L, 1L, 10);

            assertThat(inventory.getStock()).isEqualTo(40);
            then(branchInventoryRepository).should().save(inventory);
        }

        @Test
        @DisplayName("validateAndReduceStock - should throw exception when inventory not found")
        void validateAndReduceStock_NotFound() {
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 99L))
                    .willReturn(Optional.empty());
            assertThatThrownBy(() -> inventoryService.validateAndReduceStock(1L, 99L, 5))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product 99 not found in branch 1");
        }

        @Test
        @DisplayName("validateAndReduceStock - should throw exception when insufficient stock")
        void validateAndReduceStock_InsufficientStock() {
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                    .willReturn(Optional.of(inventory));
            assertThatThrownBy(() -> inventoryService.validateAndReduceStock(1L, 1L, 100))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Available: 50, required: 100");
        }

        @Test
        @DisplayName("restoreStock - should increase stock")
        void restoreStock_Success() {
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                    .willReturn(Optional.of(inventory));
            given(branchInventoryRepository.save(any(BranchInventory.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            inventoryService.restoreStock(1L, 1L, 15);

            assertThat(inventory.getStock()).isEqualTo(65);
            verify(branchInventoryRepository).save(inventory);
        }
    }

    @Nested
    @DisplayName("Batch operations")
    class BatchOperations {
        private List<SaleDetailRequest> detailRequests;

        @BeforeEach
        void setUpBatch() {
            detailRequests = List.of(
                    SaleDetailRequest.builder().productId(1L).quantity(5).build(),
                    SaleDetailRequest.builder().productId(2L).quantity(3).build()
            );
        }

        @Test
        @DisplayName("validateAndReduceStockBatch - should reduce stock for all products")
        void validateAndReduceStockBatch_Success() {
            Product product2 = Product.builder().id(2L).name("Product 2").category("Test").build();
            BranchInventory inv1 = BranchInventory.builder()
                    .id(100L).branch(branch).product(product).stock(20).minStock(5).build();
            BranchInventory inv2 = BranchInventory.builder()
                    .id(101L).branch(branch).product(product2).stock(10).minStock(5).build();
            given(branchInventoryRepository.findByBranchIdAndProductIdIn(1L, Set.of(1L, 2L)))
                    .willReturn(List.of(inv1, inv2));
            inventoryService.validateAndReduceStockBatch(1L, detailRequests);
            assertThat(inv1.getStock()).isEqualTo(15);
            assertThat(inv2.getStock()).isEqualTo(7);
            verify(branchInventoryRepository).saveAll(List.of(inv1, inv2));
        }

        @Test
        @DisplayName("validateAndReduceStockBatch - should throw when details list is empty")
        void validateAndReduceStockBatch_EmptyDetails() {
            assertThatThrownBy(() -> inventoryService.validateAndReduceStockBatch(1L, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be empty");
        }

        @Test
        @DisplayName("validateAndReduceStockBatch - should throw when productId is null")
        void validateAndReduceStockBatch_NullProductId() {
            List<SaleDetailRequest> invalid = List.of(
                    SaleDetailRequest.builder().quantity(5).build()
            );
            assertThatThrownBy(() -> inventoryService.validateAndReduceStockBatch(1L, invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("productId");
        }

        @Test
        @DisplayName("validateAndReduceStockBatch - should throw when quantity is invalid")
        void validateAndReduceStockBatch_InvalidQuantity() {
            List<SaleDetailRequest> invalid = List.of(
                    SaleDetailRequest.builder().productId(1L).quantity(0).build()
            );
            assertThatThrownBy(() -> inventoryService.validateAndReduceStockBatch(1L, invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid quantity");
        }

        @Test
        @DisplayName("validateAndReduceStockBatch - should throw when some products not found in branch")
        void validateAndReduceStockBatch_MissingProducts() {
            given(branchInventoryRepository.findByBranchIdAndProductIdIn(1L, Set.of(1L, 2L)))
                    .willReturn(List.of(inventory));
            assertThatThrownBy(() -> inventoryService.validateAndReduceStockBatch(1L, detailRequests))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("do not exist in branch");
        }

        @Test
        @DisplayName("validateAndReduceStockBatch - should throw when insufficient stock for any product")
        void validateAndReduceStockBatch_InsufficientStock() {
            Product product2 = Product.builder().id(2L).name("Product 2").build();
            BranchInventory inv1 = BranchInventory.builder()
                    .id(100L).branch(branch).product(product).stock(2).minStock(5).build();
            BranchInventory inv2 = BranchInventory.builder()
                    .id(101L).branch(branch).product(product2).stock(10).minStock(5).build();
            given(branchInventoryRepository.findByBranchIdAndProductIdIn(1L, Set.of(1L, 2L)))
                    .willReturn(List.of(inv1, inv2));
            assertThatThrownBy(() -> inventoryService.validateAndReduceStockBatch(1L, detailRequests))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Insufficient stock for product 'Premium Rice' (ID: 1)")
                    .hasMessageContaining("Available: 2, required: 5");
        }

        @Test
        @DisplayName("restoreStockBatch - should increase stock for all products")
        void restoreStockBatch_Success() {
            Product product2 = Product.builder().id(2L).name("Product 2").build();
            SaleDetail detail1 = SaleDetail.builder().product(product).quantity(5).build();
            SaleDetail detail2 = SaleDetail.builder().product(product2).quantity(3).build();
            List<SaleDetail> details = List.of(detail1, detail2);
            BranchInventory inv1 = BranchInventory.builder()
                    .id(100L).branch(branch).product(product).stock(20).minStock(5).build();
            BranchInventory inv2 = BranchInventory.builder()
                    .id(101L).branch(branch).product(product2).stock(10).minStock(5).build();
            given(branchInventoryRepository.findByBranchIdAndProductIdIn(1L, Set.of(1L, 2L)))
                    .willReturn(List.of(inv1, inv2));
            inventoryService.restoreStockBatch(1L, details);
            assertThat(inv1.getStock()).isEqualTo(25);
            assertThat(inv2.getStock()).isEqualTo(13);
            verify(branchInventoryRepository).saveAll(List.of(inv1, inv2));
        }

        @Test
        @DisplayName("restoreStockBatch - should do nothing when details list is empty")
        void restoreStockBatch_EmptyDetails() {
            inventoryService.restoreStockBatch(1L, List.of());
            then(branchInventoryRepository).should(never()).saveAll(any());
        }
    }

    @Nested
    @DisplayName("Stock queries (extended)")
    class StockQueriesExtended {
        @Test
        @DisplayName("getMinStockInBranch - should return minStock when inventory exists")
        void getMinStockInBranch_WhenExists_ShouldReturnMinStock() {
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                    .willReturn(Optional.of(inventory));

            Integer minStock = inventoryService.getMinStockInBranch(1L, 1L);

            assertThat(minStock).isEqualTo(5);
        }

        @Test
        @DisplayName("getMinStockInBranch - should return 0 when inventory not found")
        void getMinStockInBranch_WhenNotFound_ShouldReturnZero() {
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 99L))
                    .willReturn(Optional.empty());

            Integer minStock = inventoryService.getMinStockInBranch(1L, 99L);

            assertThat(minStock).isZero();
        }

        @Test
        @DisplayName("getTotalStockByProduct - should return sum of stock across branches")
        void getTotalStockByProduct_ShouldReturnSum() {
            BranchInventory inv2 = BranchInventory.builder()
                    .branch(branch)
                    .product(product)
                    .stock(30)
                    .minStock(5)
                    .build();
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(branchInventoryRepository.findByProductId(1L))
                    .willReturn(List.of(inventory, inv2));

            TotalStockResponse result = inventoryService.getTotalStockByProduct(1L);

            assertThat(result.getProductId()).isEqualTo(1L);
            assertThat(result.getProductName()).isEqualTo("Premium Rice");
            assertThat(result.getTotalStock()).isEqualTo(80L);
        }

        @Test
        @DisplayName("getTotalStockByProduct - should throw when product not found")
        void getTotalStockByProduct_WhenProductNotFound_ShouldThrow() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.getTotalStockByProduct(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found with id: 99");
        }
    }

    @Nested
    @DisplayName("Stock mutations")
    class StockMutations {
        @Test
        @DisplayName("updateStock - should set stock and minStock, and update lastRestockDate when stock increases")
        void updateStock_WhenStockIncreases_ShouldUpdateAndSetLastRestockDate() {
            StockUpdateRequest request = StockUpdateRequest.builder()
                    .stock(70)
                    .minStock(10)
                    .build();
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                    .willReturn(Optional.of(inventory));
            given(branchInventoryRepository.save(any(BranchInventory.class)))
                    .willAnswer(inv -> inv.getArgument(0));
            given(branchInventoryMapper.toResponse(any(BranchInventory.class)))
                    .willAnswer(inv -> {
                        BranchInventory saved = inv.getArgument(0);
                        return BranchInventoryResponse.builder()
                                .id(saved.getId())
                                .branchId(1L)
                                .productId(1L)
                                .stock(saved.getStock())
                                .minStock(saved.getMinStock())
                                .lastRestockDate(saved.getLastRestockDate())
                                .build();
                    });

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            BranchInventoryResponse result = inventoryService.updateStock(1L, 1L, request);

            assertThat(result.getStock()).isEqualTo(70);
            assertThat(result.getMinStock()).isEqualTo(10);
            assertThat(result.getLastRestockDate()).isAfter(before);
            then(branchInventoryRepository).should().save(inventory);
        }

        @Test
        @DisplayName("updateStock - should not update lastRestockDate when stock decreases")
        void updateStock_WhenStockDecreases_ShouldNotUpdateLastRestockDate() {
            LocalDateTime originalRestock = inventory.getLastRestockDate();
            StockUpdateRequest request = StockUpdateRequest.builder()
                    .stock(30)
                    .minStock(5)
                    .build();
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                    .willReturn(Optional.of(inventory));
            given(branchInventoryRepository.save(any(BranchInventory.class)))
                    .willAnswer(inv -> inv.getArgument(0));
            given(branchInventoryMapper.toResponse(any(BranchInventory.class)))
                    .willAnswer(inv -> {
                        BranchInventory saved = inv.getArgument(0);
                        return BranchInventoryResponse.builder()
                                .id(saved.getId())
                                .stock(saved.getStock())
                                .minStock(saved.getMinStock())
                                .lastRestockDate(saved.getLastRestockDate())
                                .build();
                    });

            BranchInventoryResponse result = inventoryService.updateStock(1L, 1L, request);

            assertThat(result.getStock()).isEqualTo(30);
            assertThat(result.getLastRestockDate()).isEqualTo(originalRestock);
        }

        @Test
        @DisplayName("updateStock - should throw when inventory not found")
        void updateStock_WhenNotFound_ShouldThrow() {
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 99L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.updateStock(1L, 99L,
                    StockUpdateRequest.builder().stock(10).minStock(5).build()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product 99 not found in branch 1");
        }

        @Test
        @DisplayName("adjustStock - should increase stock and set lastRestockDate when delta positive")
        void adjustStock_PositiveDelta_ShouldIncreaseAndSetLastRestockDate() {
            StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                    .delta(15)
                    .reason("Manual restock")
                    .build();
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                    .willReturn(Optional.of(inventory));
            given(branchInventoryRepository.save(any(BranchInventory.class)))
                    .willAnswer(inv -> inv.getArgument(0));
            given(branchInventoryMapper.toResponse(any(BranchInventory.class)))
                    .willAnswer(inv -> {
                        BranchInventory saved = inv.getArgument(0);
                        return BranchInventoryResponse.builder()
                                .id(saved.getId())
                                .stock(saved.getStock())
                                .lastRestockDate(saved.getLastRestockDate())
                                .build();
                    });

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            BranchInventoryResponse result = inventoryService.adjustStock(1L, 1L, request);

            assertThat(result.getStock()).isEqualTo(65);
            assertThat(result.getLastRestockDate()).isAfter(before);
        }

        @Test
        @DisplayName("adjustStock - should decrease stock without updating lastRestockDate when delta negative")
        void adjustStock_NegativeDelta_ShouldDecreaseWithoutUpdatingLastRestockDate() {
            LocalDateTime originalRestock = inventory.getLastRestockDate();
            StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                    .delta(-10)
                    .reason("Adjustment")
                    .build();
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                    .willReturn(Optional.of(inventory));
            given(branchInventoryRepository.save(any(BranchInventory.class)))
                    .willAnswer(inv -> inv.getArgument(0));
            given(branchInventoryMapper.toResponse(any(BranchInventory.class)))
                    .willAnswer(inv -> {
                        BranchInventory saved = inv.getArgument(0);
                        return BranchInventoryResponse.builder()
                                .stock(saved.getStock())
                                .lastRestockDate(saved.getLastRestockDate())
                                .build();
                    });

            BranchInventoryResponse result = inventoryService.adjustStock(1L, 1L, request);

            assertThat(result.getStock()).isEqualTo(40);
            assertThat(result.getLastRestockDate()).isEqualTo(originalRestock);
        }

        @Test
        @DisplayName("adjustStock - should throw InsufficientStockException when result would be negative")
        void adjustStock_WouldGoNegative_ShouldThrow() {
            StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                    .delta(-100)
                    .reason("Over-adjust")
                    .build();
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                    .willReturn(Optional.of(inventory));

            assertThatThrownBy(() -> inventoryService.adjustStock(1L, 1L, request))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Adjustment would result in negative stock");
        }

        @Test
        @DisplayName("increaseStock - should increase stock via adjustStock")
        void increaseStock_Success() {
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                    .willReturn(Optional.of(inventory));
            given(branchInventoryRepository.save(any(BranchInventory.class)))
                    .willAnswer(inv -> inv.getArgument(0));
            given(branchInventoryMapper.toResponse(any(BranchInventory.class)))
                    .willAnswer(inv -> BranchInventoryResponse.builder()
                            .stock(((BranchInventory) inv.getArgument(0)).getStock())
                            .build());

            inventoryService.increaseStock(1L, 1L, 20);

            assertThat(inventory.getStock()).isEqualTo(70);
            then(branchInventoryRepository).should().save(inventory);
        }

        @Test
        @DisplayName("increaseStock - should throw InvalidOperationException when quantity is null or <= 0")
        void increaseStock_InvalidQuantity_ShouldThrow() {
            assertThatThrownBy(() -> inventoryService.increaseStock(1L, 1L, null))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("Quantity must be positive");

            assertThatThrownBy(() -> inventoryService.increaseStock(1L, 1L, 0))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("Quantity must be positive");

            assertThatThrownBy(() -> inventoryService.increaseStock(1L, 1L, -5))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("Quantity must be positive");
        }
    }

    @Nested
    @DisplayName("Auto-provisioning")
    class AutoProvisioning {
        @Test
        @DisplayName("initializeInventoryForNewProduct - should create inventory entries for all branches with stock 0 and minStock 5")
        void initializeInventoryForNewProduct_ShouldCreateEntries() {
            Branch branch2 = Branch.builder().id(2L).name("North Branch").build();
            given(branchRepository.findAll()).willReturn(List.of(branch, branch2));
            given(branchInventoryRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

            inventoryService.initializeInventoryForNewProduct(product);

            then(branchInventoryRepository).should().saveAll(any());
            verify(branchInventoryRepository).saveAll(org.mockito.ArgumentMatchers.argThat(list -> {
                List<BranchInventory> inventories = (List<BranchInventory>) list;
                return inventories.size() == 2
                        && inventories.stream().allMatch(i -> i.getStock() == 0 && i.getMinStock() == 5)
                        && inventories.stream().allMatch(i -> i.getProduct().equals(product));
            }));
        }

        @Test
        @DisplayName("initializeInventoryForNewBranch - should create inventory entries for all products with stock 0 and minStock 5")
        void initializeInventoryForNewBranch_ShouldCreateEntries() {
            Product product2 = Product.builder().id(2L).name("Product 2").category("Test").build();
            given(productRepository.findAll()).willReturn(List.of(product, product2));
            given(branchInventoryRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

            inventoryService.initializeInventoryForNewBranch(branch);

            then(branchInventoryRepository).should().saveAll(any());
            verify(branchInventoryRepository).saveAll(org.mockito.ArgumentMatchers.argThat(list -> {
                List<BranchInventory> inventories = (List<BranchInventory>) list;
                return inventories.size() == 2
                        && inventories.stream().allMatch(i -> i.getStock() == 0 && i.getMinStock() == 5)
                        && inventories.stream().allMatch(i -> i.getBranch().equals(branch));
            }));
        }
    }

    @Nested
    @DisplayName("Concurrency (Optimistic Locking)")
    class Concurrency {
        @Test
        @DisplayName("validateAndReduceStock - should propagate OptimisticLockingFailure when version conflict")
        void validateAndReduceStock_OptimisticLockingFailure() {
            given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                    .willReturn(Optional.of(inventory));
            given(branchInventoryRepository.save(any(BranchInventory.class)))
                    .willThrow(ObjectOptimisticLockingFailureException.class);
            assertThatThrownBy(() -> inventoryService.validateAndReduceStock(1L, 1L, 5))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        }

        @Test
        @DisplayName("validateAndReduceStockBatch - should propagate OptimisticLockingFailure when version conflict on saveAll")
        void validateAndReduceStockBatch_OptimisticLockingFailure() {
            List<SaleDetailRequest> requests = List.of(
                    SaleDetailRequest.builder().productId(1L).quantity(5).build()
            );
            given(branchInventoryRepository.findByBranchIdAndProductIdIn(1L, Set.of(1L)))
                    .willReturn(List.of(inventory));
            given(branchInventoryRepository.saveAll(any()))
                    .willThrow(ObjectOptimisticLockingFailureException.class);
            assertThatThrownBy(() -> inventoryService.validateAndReduceStockBatch(1L, requests))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        }
    }
}