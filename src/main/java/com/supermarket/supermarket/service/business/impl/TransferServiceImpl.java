package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.transfer.RejectTransferRequest;
import com.supermarket.supermarket.dto.transfer.TransferRequest;
import com.supermarket.supermarket.dto.transfer.TransferResponse;
import com.supermarket.supermarket.exception.InsufficientPermissionsException;
import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.TransferMapper;
import com.supermarket.supermarket.model.branch.Branch;
import com.supermarket.supermarket.model.product.Product;
import com.supermarket.supermarket.model.transfer.StockTransfer;
import com.supermarket.supermarket.model.transfer.TransferStatus;
import com.supermarket.supermarket.model.user.User;
import com.supermarket.supermarket.model.user.UserRole;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.repository.StockTransferRepository;
import com.supermarket.supermarket.security.SecurityUtils;
import com.supermarket.supermarket.service.business.InventoryService;
import com.supermarket.supermarket.service.business.NotificationEventService;
import com.supermarket.supermarket.service.business.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransferServiceImpl implements TransferService {
    private final StockTransferRepository transferRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final TransferMapper transferMapper;
    private final SecurityUtils securityUtils;
    private final NotificationEventService notificationEventService;

    @Override
    public TransferResponse requestTransfer(TransferRequest request) {
        log.info("Requesting transfer: source={}, target={}, product={}, quantity={}",
                request.getSourceBranchId(), request.getTargetBranchId(),
                request.getProductId(), request.getQuantity());
        if (request.getSourceBranchId().equals(request.getTargetBranchId())) {
            throw new InvalidOperationException("Source and target branches must be different");
        }
        Branch source = branchRepository.findById(request.getSourceBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Source branch not found"));
        Branch target = branchRepository.findById(request.getTargetBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Target branch not found"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Integer availableStock = inventoryService.getStockInBranch(source.getId(), product.getId());
        if (availableStock < request.getQuantity()) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock in source branch. Available: %d, requested: %d",
                            availableStock, request.getQuantity()));
        }
        User currentUser = getCurrentUser();
        StockTransfer transfer = StockTransfer.builder()
                .sourceBranch(source)
                .targetBranch(target)
                .product(product)
                .quantity(request.getQuantity())
                .status(TransferStatus.PENDING)
                .requestedBy(currentUser)
                .requestedAt(LocalDateTime.now())
                .build();
        StockTransfer saved = transferRepository.save(transfer);
        notificationEventService.onTransferRequested(saved);
        log.info("Transfer requested with id: {}", saved.getId());
        return transferMapper.toResponse(saved);
    }

    @Override
    public TransferResponse approveTransfer(Long transferId) {
        log.info("Approving transfer id: {}", transferId);
        StockTransfer transfer = findTransfer(transferId);
        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new InvalidOperationException("Only PENDING transfers can be approved");
        }
        User currentUser = getCurrentUser();
        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setApprovedBy(currentUser);
        transfer.setApprovedAt(LocalDateTime.now());
        StockTransfer saved = transferRepository.save(transfer);
        notificationEventService.onTransferApproved(saved);
        log.info("Transfer approved with id: {}", transferId);
        return transferMapper.toResponse(saved);
    }

    @Override
    public TransferResponse rejectTransfer(Long transferId, RejectTransferRequest request) {
        log.info("Rejecting transfer id: {}", transferId);
        StockTransfer transfer = findTransfer(transferId);
        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new InvalidOperationException("Only PENDING transfers can be rejected");
        }
        User currentUser = getCurrentUser();
        transfer.setStatus(TransferStatus.REJECTED);
        transfer.setApprovedBy(currentUser);
        transfer.setApprovedAt(LocalDateTime.now());
        transfer.setRejectionReason(request.getReason());
        StockTransfer saved = transferRepository.save(transfer);
        notificationEventService.onTransferRejected(saved);
        log.info("Transfer rejected with id: {}", transferId);
        return transferMapper.toResponse(saved);
    }

    @Override
    public TransferResponse completeTransfer(Long transferId) {
        log.info("Completing transfer id: {}", transferId);
        StockTransfer transfer = findTransfer(transferId);
        if (transfer.getStatus() != TransferStatus.APPROVED) {
            throw new InvalidOperationException("Only APPROVED transfers can be completed");
        }
        Integer availableStock = inventoryService.getStockInBranch(
                transfer.getSourceBranch().getId(), transfer.getProduct().getId());
        if (availableStock < transfer.getQuantity()) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock to complete transfer. Available: %d, required: %d",
                            availableStock, transfer.getQuantity()));
        }
        if (!branchRepository.existsById(transfer.getTargetBranch().getId())) {
            throw new ResourceNotFoundException("Target branch no longer exists");
        }
        inventoryService.validateAndReduceStock(
                transfer.getSourceBranch().getId(),
                transfer.getProduct().getId(),
                transfer.getQuantity());
        inventoryService.increaseStock(
                transfer.getTargetBranch().getId(),
                transfer.getProduct().getId(),
                transfer.getQuantity());
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setCompletedAt(LocalDateTime.now());
        StockTransfer saved = transferRepository.save(transfer);
        notificationEventService.onTransferCompleted(saved);
        log.info("Transfer completed with id: {}", transferId);
        return transferMapper.toResponse(saved);
    }

    @Override
    public TransferResponse cancelTransfer(Long transferId) {
        log.info("Cancelling transfer id: {}", transferId);
        StockTransfer transfer = findTransfer(transferId);
        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new InvalidOperationException("Only PENDING transfers can be cancelled");
        }
        User currentUser = getCurrentUser();
        boolean isRequester = currentUser.getId().equals(transfer.getRequestedBy().getId());
        boolean isAdmin = currentUser.getRole().equals(UserRole.ADMIN);
        if (!isRequester && !isAdmin) {
            throw new InsufficientPermissionsException("You are not allowed to cancel this transfer");
        }
        transfer.setStatus(TransferStatus.CANCELLED);
        StockTransfer saved = transferRepository.save(transfer);
        log.info("Transfer cancelled with id: {}", transferId);
        return transferMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferResponse> getAllTransfers() {
        return transferMapper.toResponseList(transferRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponse getTransferById(Long id) {
        return transferMapper.toResponse(findTransfer(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferResponse> getTransfersByStatus(String status) {
        try {
            TransferStatus transferStatus = TransferStatus.valueOf(status.toUpperCase());
            return transferMapper.toResponseList(transferRepository.findByStatus(transferStatus));
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("Invalid transfer status: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferResponse> getTransfersBySourceBranch(Long branchId) {
        if (!branchRepository.existsById(branchId)) {
            throw new ResourceNotFoundException("Branch not found with id: " + branchId);
        }
        return transferMapper.toResponseList(transferRepository.findBySourceBranchId(branchId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferResponse> getTransfersByTargetBranch(Long branchId) {
        if (!branchRepository.existsById(branchId)) {
            throw new ResourceNotFoundException("Branch not found with id: " + branchId);
        }
        return transferMapper.toResponseList(transferRepository.findByTargetBranchId(branchId));
    }

    private StockTransfer findTransfer(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + id));
    }

    private User getCurrentUser() {
        return securityUtils.getCurrentUser();
    }
}