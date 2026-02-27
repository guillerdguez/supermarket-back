package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.dto.transfer.RejectTransferRequest;
import com.supermarket.supermarket.dto.transfer.TransferRequest;
import com.supermarket.supermarket.dto.transfer.TransferResponse;
import com.supermarket.supermarket.exception.InsufficientPermissionsException;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.fixtures.branch.BranchFixtures;
import com.supermarket.supermarket.fixtures.product.ProductFixtures;
import com.supermarket.supermarket.fixtures.transfer.TransferFixtures;
import com.supermarket.supermarket.fixtures.user.UserFixtures;
import com.supermarket.supermarket.mapper.TransferMapper;
import com.supermarket.supermarket.model.Branch;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.StockTransfer;
import com.supermarket.supermarket.model.TransferStatus;
import com.supermarket.supermarket.model.User;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.repository.StockTransferRepository;
import com.supermarket.supermarket.security.SecurityUtils;
import com.supermarket.supermarket.service.business.InventoryService;
import com.supermarket.supermarket.service.business.NotificationEventService;
import com.supermarket.supermarket.service.business.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private StockTransferRepository transferRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private TransferMapper transferMapper;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private NotificationEventService notificationEventService;
    
    @InjectMocks
    private TransferServiceImpl transferService;

    private User cashier;
    private User manager;
    private User admin;
    private Branch sourceBranch;
    private Branch targetBranch;
    private Product product;

    @BeforeEach
    void setUp() {
        cashier = UserFixtures.defaultCashier();
        manager = UserFixtures.defaultManager();
        admin = UserFixtures.defaultAdmin();
        sourceBranch = BranchFixtures.defaultBranch();
        targetBranch = Branch.builder().id(2L).name("North Branch").address("456 North Ave").build();
        product = ProductFixtures.defaultProduct();
    }

    @Nested
    @DisplayName("requestTransfer")
    class RequestTransfer {
        @Test
        @DisplayName("should create PENDING transfer when stock is sufficient")
        void requestTransfer_Success() {
            given(securityUtils.getCurrentUser()).willReturn(cashier);

            TransferRequest request = TransferFixtures.validTransferRequest();
            TransferResponse expected = TransferResponse.builder().id(1L).status(TransferStatus.PENDING).build();

            given(branchRepository.findById(1L)).willReturn(Optional.of(sourceBranch));
            given(branchRepository.findById(2L)).willReturn(Optional.of(targetBranch));
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(inventoryService.getStockInBranch(1L, 1L)).willReturn(20);
            given(transferRepository.save(any(StockTransfer.class))).willAnswer(inv -> {
                StockTransfer t = inv.getArgument(0);
                t.setId(1L);
                return t;
            });
            given(transferMapper.toResponse(any())).willReturn(expected);

            TransferResponse result = transferService.requestTransfer(request);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(TransferStatus.PENDING);
            then(transferRepository).should().save(any(StockTransfer.class));
        }

        @Test
        @DisplayName("should throw InvalidOperationException when source and target are the same branch")
        void requestTransfer_SameBranch_Throws() {
            TransferRequest request = TransferFixtures.sameBranchRequest();

            assertThatThrownBy(() -> transferService.requestTransfer(request))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("different");

            then(branchRepository).shouldHaveNoInteractions();
            then(transferRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when source branch does not exist")
        void requestTransfer_SourceBranchNotFound_Throws() {
            TransferRequest request = TransferFixtures.validTransferRequest();
            given(branchRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> transferService.requestTransfer(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Source branch");

            then(transferRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when target branch does not exist")
        void requestTransfer_TargetBranchNotFound_Throws() {
            TransferRequest request = TransferFixtures.validTransferRequest();
            given(branchRepository.findById(1L)).willReturn(Optional.of(sourceBranch));
            given(branchRepository.findById(2L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> transferService.requestTransfer(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Target branch");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product does not exist")
        void requestTransfer_ProductNotFound_Throws() {
            TransferRequest request = TransferFixtures.validTransferRequest();
            given(branchRepository.findById(1L)).willReturn(Optional.of(sourceBranch));
            given(branchRepository.findById(2L)).willReturn(Optional.of(targetBranch));
            given(productRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> transferService.requestTransfer(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product");
        }

        @Test
        @DisplayName("should throw InsufficientStockException when source has not enough stock")
        void requestTransfer_InsufficientStock_Throws() {
            TransferRequest request = TransferFixtures.validTransferRequest();
            given(branchRepository.findById(1L)).willReturn(Optional.of(sourceBranch));
            given(branchRepository.findById(2L)).willReturn(Optional.of(targetBranch));
            given(productRepository.findById(1L)).willReturn(Optional.of(product));
            given(inventoryService.getStockInBranch(1L, 1L)).willReturn(5);

            assertThatThrownBy(() -> transferService.requestTransfer(request))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Available: 5, requested: 10");

            then(transferRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("approveTransfer")
    class ApproveTransfer {
        @Test
        @DisplayName("should approve a PENDING transfer and set approvedBy")
        void approveTransfer_Success() {
            given(securityUtils.getCurrentUser()).willReturn(manager);

            StockTransfer transfer = buildTransfer(TransferStatus.PENDING);
            TransferResponse expected = TransferResponse.builder().id(1L).status(TransferStatus.APPROVED).build();

            given(transferRepository.findById(1L)).willReturn(Optional.of(transfer));
            given(transferRepository.save(transfer)).willReturn(transfer);
            given(transferMapper.toResponse(transfer)).willReturn(expected);

            TransferResponse result = transferService.approveTransfer(1L);

            assertThat(result.getStatus()).isEqualTo(TransferStatus.APPROVED);
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.APPROVED);
            assertThat(transfer.getApprovedBy()).isEqualTo(manager);
            assertThat(transfer.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw InvalidOperationException when transfer is not PENDING")
        void approveTransfer_NotPending_Throws() {
            StockTransfer transfer = buildTransfer(TransferStatus.COMPLETED);
            given(transferRepository.findById(1L)).willReturn(Optional.of(transfer));

            assertThatThrownBy(() -> transferService.approveTransfer(1L))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when transfer does not exist")
        void approveTransfer_TransferNotFound_Throws() {
            given(transferRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> transferService.approveTransfer(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("rejectTransfer")
    class RejectTransfer {
        @Test
        @DisplayName("should reject a PENDING transfer and store reason")
        void rejectTransfer_Success() {
            given(securityUtils.getCurrentUser()).willReturn(manager);

            StockTransfer transfer = buildTransfer(TransferStatus.PENDING);
            RejectTransferRequest request = TransferFixtures.validRejectRequest();
            TransferResponse expected = TransferResponse.builder().id(1L).status(TransferStatus.REJECTED).build();

            given(transferRepository.findById(1L)).willReturn(Optional.of(transfer));
            given(transferRepository.save(transfer)).willReturn(transfer);
            given(transferMapper.toResponse(transfer)).willReturn(expected);

            TransferResponse result = transferService.rejectTransfer(1L, request);

            assertThat(result.getStatus()).isEqualTo(TransferStatus.REJECTED);
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.REJECTED);
            assertThat(transfer.getRejectionReason()).isEqualTo(request.getReason());
            assertThat(transfer.getApprovedBy()).isEqualTo(manager);
        }

        @Test
        @DisplayName("should throw InvalidOperationException when transfer is not PENDING")
        void rejectTransfer_NotPending_Throws() {
            StockTransfer transfer = buildTransfer(TransferStatus.APPROVED);
            given(transferRepository.findById(1L)).willReturn(Optional.of(transfer));

            assertThatThrownBy(() -> transferService.rejectTransfer(1L,
                    RejectTransferRequest.builder().reason("some reason").build()))
                    .isInstanceOf(InvalidOperationException.class);
        }
    }

    @Nested
    @DisplayName("completeTransfer")
    class CompleteTransfer {
        @Test
        @DisplayName("should complete transfer, reduce source stock and increase target stock")
        void completeTransfer_Success() {

            StockTransfer transfer = buildTransfer(TransferStatus.APPROVED);
            TransferResponse expected = TransferResponse.builder().id(1L).status(TransferStatus.COMPLETED).build();

            given(transferRepository.findById(1L)).willReturn(Optional.of(transfer));
            given(inventoryService.getStockInBranch(1L, 1L)).willReturn(20);
            given(branchRepository.existsById(2L)).willReturn(true);
            given(transferRepository.save(transfer)).willReturn(transfer);
            given(transferMapper.toResponse(transfer)).willReturn(expected);

            TransferResponse result = transferService.completeTransfer(1L);

            assertThat(result.getStatus()).isEqualTo(TransferStatus.COMPLETED);
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.COMPLETED);
            assertThat(transfer.getCompletedAt()).isNotNull();
            then(inventoryService).should().validateAndReduceStock(1L, 1L, 10);
            then(inventoryService).should().increaseStock(2L, 1L, 10);
        }

        @Test
        @DisplayName("should throw InvalidOperationException when transfer is not APPROVED")
        void completeTransfer_NotApproved_Throws() {
            StockTransfer transfer = buildTransfer(TransferStatus.PENDING);
            given(transferRepository.findById(1L)).willReturn(Optional.of(transfer));

            assertThatThrownBy(() -> transferService.completeTransfer(1L))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("APPROVED");

            then(inventoryService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("should throw InsufficientStockException when source stock dropped since approval")
        void completeTransfer_StockDroppedSinceApproval_Throws() {
            StockTransfer transfer = buildTransfer(TransferStatus.APPROVED);
            given(transferRepository.findById(1L)).willReturn(Optional.of(transfer));
            given(inventoryService.getStockInBranch(1L, 1L)).willReturn(5);

            assertThatThrownBy(() -> transferService.completeTransfer(1L))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Available: 5, required: 10");

            then(inventoryService).should(never()).validateAndReduceStock(any(), any(), any());
            then(inventoryService).should(never()).increaseStock(any(), any(), any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when target branch no longer exists")
        void completeTransfer_TargetBranchMissing_Throws() {
            StockTransfer transfer = buildTransfer(TransferStatus.APPROVED);
            given(transferRepository.findById(1L)).willReturn(Optional.of(transfer));
            given(inventoryService.getStockInBranch(1L, 1L)).willReturn(20);
            given(branchRepository.existsById(2L)).willReturn(false);

            assertThatThrownBy(() -> transferService.completeTransfer(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Target branch no longer exists");
        }
    }

    @Nested
    @DisplayName("cancelTransfer")
    class CancelTransfer {
        @Test
        @DisplayName("should allow requester to cancel their own PENDING transfer")
        void cancelTransfer_ByRequester_Success() {
            given(securityUtils.getCurrentUser()).willReturn(cashier);

            StockTransfer transfer = buildTransfer(TransferStatus.PENDING);
            TransferResponse expected = TransferResponse.builder().id(1L).status(TransferStatus.CANCELLED).build();

            given(transferRepository.findById(1L)).willReturn(Optional.of(transfer));
            given(transferRepository.save(transfer)).willReturn(transfer);
            given(transferMapper.toResponse(transfer)).willReturn(expected);

            TransferResponse result = transferService.cancelTransfer(1L);

            assertThat(result.getStatus()).isEqualTo(TransferStatus.CANCELLED);
            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.CANCELLED);
        }

        @Test
        @DisplayName("should allow ADMIN to cancel any PENDING transfer")
        void cancelTransfer_ByAdmin_Success() {
            given(securityUtils.getCurrentUser()).willReturn(admin);

            StockTransfer transfer = buildTransfer(TransferStatus.PENDING);
            TransferResponse expected = TransferResponse.builder().id(1L).status(TransferStatus.CANCELLED).build();

            given(transferRepository.findById(1L)).willReturn(Optional.of(transfer));
            given(transferRepository.save(transfer)).willReturn(transfer);
            given(transferMapper.toResponse(transfer)).willReturn(expected);

            TransferResponse result = transferService.cancelTransfer(1L);

            assertThat(transfer.getStatus()).isEqualTo(TransferStatus.CANCELLED);
        }

        @Test
        @DisplayName("should throw InsufficientPermissionsException when non-requester non-admin tries to cancel")
        void cancelTransfer_ByOtherUser_Throws() {
            given(securityUtils.getCurrentUser()).willReturn(manager);

            StockTransfer transfer = buildTransfer(TransferStatus.PENDING);
            given(transferRepository.findById(1L)).willReturn(Optional.of(transfer));

            assertThatThrownBy(() -> transferService.cancelTransfer(1L))
                    .isInstanceOf(InsufficientPermissionsException.class);

            then(transferRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidOperationException when transfer is not PENDING")
        void cancelTransfer_NotPending_Throws() {
            StockTransfer transfer = buildTransfer(TransferStatus.APPROVED);
            given(transferRepository.findById(1L)).willReturn(Optional.of(transfer));

            assertThatThrownBy(() -> transferService.cancelTransfer(1L))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("PENDING");
        }
    }

    @Nested
    @DisplayName("getTransfersByStatus")
    class GetByStatus {
        @Test
        @DisplayName("should return transfers filtered by valid status string")
        void getTransfersByStatus_ValidStatus_ReturnsList() {
            StockTransfer transfer = buildTransfer(TransferStatus.PENDING);
            given(transferRepository.findByStatus(TransferStatus.PENDING)).willReturn(List.of(transfer));
            given(transferMapper.toResponseList(List.of(transfer))).willReturn(
                    List.of(TransferResponse.builder().id(1L).status(TransferStatus.PENDING).build()));

            List<TransferResponse> result = transferService.getTransfersByStatus("pending");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(TransferStatus.PENDING);
        }

        @Test
        @DisplayName("should throw InvalidOperationException when status string is invalid")
        void getTransfersByStatus_InvalidStatus_Throws() {
            assertThatThrownBy(() -> transferService.getTransfersByStatus("FLYING"))
                    .isInstanceOf(InvalidOperationException.class)
                    .hasMessageContaining("Invalid transfer status");
        }
    }

    @Test
    @DisplayName("getTransfersBySourceBranch - should throw when branch not found")
    void getTransfersBySourceBranch_BranchNotFound_Throws() {
        given(branchRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> transferService.getTransfersBySourceBranch(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getTransfersByTargetBranch - should throw when branch not found")
    void getTransfersByTargetBranch_BranchNotFound_Throws() {
        given(branchRepository.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> transferService.getTransfersByTargetBranch(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private StockTransfer buildTransfer(TransferStatus status) {
        return StockTransfer.builder()
                .id(1L)
                .sourceBranch(sourceBranch)
                .targetBranch(targetBranch)
                .product(product)
                .quantity(10)
                .status(status)
                .requestedBy(cashier)
                .requestedAt(LocalDateTime.now())
                .build();
    }
}