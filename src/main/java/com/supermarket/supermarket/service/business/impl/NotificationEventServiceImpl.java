package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.model.cashregister.CashRegister;
import com.supermarket.supermarket.model.notification.NotificationType;
import com.supermarket.supermarket.model.sale.Sale;
import com.supermarket.supermarket.model.transfer.StockTransfer;
import com.supermarket.supermarket.model.user.User;
import com.supermarket.supermarket.model.user.UserRole;
import com.supermarket.supermarket.repository.UserRepository;
import com.supermarket.supermarket.service.business.NotificationEventService;
import com.supermarket.supermarket.service.business.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventServiceImpl implements NotificationEventService {
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    public void onLowStock(String branchName, String productName, int currentStock, int minStock) {
        String message = String.format(
                "Low stock alert: '%s' in branch '%s'. Current: %d, Minimum: %d",
                productName, branchName, currentStock, minStock);
        List<User> managers = userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.MANAGER));
        notificationService.createNotificationForUsers(managers, NotificationType.LOW_STOCK, message, null);
    }

    @Override
    public void onTransferRequested(StockTransfer transfer) {
        String message = String.format(
                "Transfer requested: %d units of '%s' from '%s' to '%s'",
                transfer.getQuantity(),
                transfer.getProduct().getName(),
                transfer.getSourceBranch().getName(),
                transfer.getTargetBranch().getName());
        List<User> managers = userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.MANAGER));
        notificationService.createNotificationForUsers(managers, NotificationType.TRANSFER_REQUESTED, message, null);
    }

    @Override
    public void onTransferApproved(StockTransfer transfer) {
        String message = String.format(
                "Your transfer request for %d units of '%s' to '%s' has been approved",
                transfer.getQuantity(),
                transfer.getProduct().getName(),
                transfer.getTargetBranch().getName());
        notificationService.createNotification(
                transfer.getRequestedBy(), NotificationType.TRANSFER_APPROVED, message, null);
    }

    @Override
    public void onTransferRejected(StockTransfer transfer) {
        String message = String.format(
                "Your transfer request for %d units of '%s' to '%s' has been rejected. Reason: %s",
                transfer.getQuantity(),
                transfer.getProduct().getName(),
                transfer.getTargetBranch().getName(),
                transfer.getRejectionReason());
        notificationService.createNotification(
                transfer.getRequestedBy(), NotificationType.TRANSFER_REJECTED, message, null);
    }

    @Override
    public void onTransferCompleted(StockTransfer transfer) {
        String message = String.format(
                "Transfer completed: %d units of '%s' moved from '%s' to '%s'",
                transfer.getQuantity(),
                transfer.getProduct().getName(),
                transfer.getSourceBranch().getName(),
                transfer.getTargetBranch().getName());
        List<User> managers = userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.MANAGER));
        notificationService.createNotificationForUsers(managers, NotificationType.TRANSFER_COMPLETED, message, null);
    }

    @Override
    public void onCashRegisterDiscrepancy(CashRegister register, BigDecimal variance) {
        String message = String.format(
                "Cash register discrepancy detected in branch '%s'. Variance: %.2f",
                register.getBranch().getName(),
                variance);
        List<User> managers = userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.MANAGER));
        notificationService.createNotificationForUsers(
                managers, NotificationType.CASH_REGISTER_DISCREPANCY, message, null);
    }

    @Override
    public void onSaleCancelled(Sale sale) {
        String message = String.format(
                "Sale #%d in branch '%s' has been cancelled. Reason: %s",
                sale.getId(),
                sale.getBranch().getName(),
                sale.getCancellationReason());
        List<User> managers = userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.MANAGER));
        notificationService.createNotificationForUsers(managers, NotificationType.SALE_CANCELLED, message, null);
    }
}