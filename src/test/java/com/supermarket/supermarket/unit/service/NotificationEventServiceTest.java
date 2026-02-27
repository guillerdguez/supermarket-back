package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.fixtures.cashregister.CashRegisterFixtures;
import com.supermarket.supermarket.fixtures.sale.SaleFixtures;
import com.supermarket.supermarket.fixtures.transfer.TransferFixtures;
import com.supermarket.supermarket.fixtures.user.UserFixtures;
import com.supermarket.supermarket.model.CashRegister;
import com.supermarket.supermarket.model.NotificationType;
import com.supermarket.supermarket.model.Sale;
import com.supermarket.supermarket.model.SaleStatus;
import com.supermarket.supermarket.model.StockTransfer;
import com.supermarket.supermarket.model.TransferStatus;
import com.supermarket.supermarket.model.User;
import com.supermarket.supermarket.model.UserRole;
import com.supermarket.supermarket.repository.UserRepository;
import com.supermarket.supermarket.service.business.NotificationService;
import com.supermarket.supermarket.service.business.impl.NotificationEventServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationEventServiceTest {
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private NotificationEventServiceImpl notificationEventService;

    @Test
    @DisplayName("onLowStock - should notify admins and managers")
    void onLowStock_ShouldNotifyManagers() {
        List<User> managers = List.of(UserFixtures.defaultAdmin(), UserFixtures.defaultManager());
        given(userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.MANAGER)))
                .willReturn(managers);

        notificationEventService.onLowStock("Central Branch", "Rice", 3, 10);

        then(notificationService).should().createNotificationForUsers(
                eq(managers),
                eq(NotificationType.LOW_STOCK),
                argThat(msg -> msg.contains("Rice") && msg.contains("Central Branch") && msg.contains("3")),
                isNull());
    }

    @Test
    @DisplayName("onTransferRequested - should notify admins and managers")
    void onTransferRequested_ShouldNotifyManagers() {
        List<User> managers = List.of(UserFixtures.defaultAdmin(), UserFixtures.defaultManager());
        given(userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.MANAGER)))
                .willReturn(managers);
        StockTransfer transfer = TransferFixtures.pendingTransfer();

        notificationEventService.onTransferRequested(transfer);

        then(notificationService).should().createNotificationForUsers(
                eq(managers),
                eq(NotificationType.TRANSFER_REQUESTED),
                argThat(msg -> msg.contains("Premium Rice")),
                isNull());
    }

    @Test
    @DisplayName("onTransferApproved - should notify requester only")
    void onTransferApproved_ShouldNotifyRequester() {
        StockTransfer transfer = TransferFixtures.approvedTransfer();

        notificationEventService.onTransferApproved(transfer);

        then(notificationService).should().createNotification(
                eq(transfer.getRequestedBy()),
                eq(NotificationType.TRANSFER_APPROVED),
                argThat(msg -> msg.contains("approved")),
                isNull());
        then(notificationService).should(never()).createNotificationForUsers(any(), any(), any(), any());
    }

    @Test
    @DisplayName("onTransferRejected - should notify requester with reason")
    void onTransferRejected_ShouldNotifyRequesterWithReason() {
        StockTransfer transfer = TransferFixtures.pendingTransfer();
        transfer.setStatus(TransferStatus.REJECTED);
        transfer.setRejectionReason("Not enough demand");

        notificationEventService.onTransferRejected(transfer);

        then(notificationService).should().createNotification(
                eq(transfer.getRequestedBy()),
                eq(NotificationType.TRANSFER_REJECTED),
                argThat(msg -> msg.contains("rejected") && msg.contains("Not enough demand")),
                isNull());
    }

    @Test
    @DisplayName("onTransferCompleted - should notify admins and managers")
    void onTransferCompleted_ShouldNotifyManagers() {
        List<User> managers = List.of(UserFixtures.defaultAdmin(), UserFixtures.defaultManager());
        given(userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.MANAGER)))
                .willReturn(managers);
        StockTransfer transfer = TransferFixtures.approvedTransfer();
        transfer.setStatus(TransferStatus.COMPLETED);

        notificationEventService.onTransferCompleted(transfer);

        then(notificationService).should().createNotificationForUsers(
                eq(managers),
                eq(NotificationType.TRANSFER_COMPLETED),
                argThat(msg -> msg.contains("completed")),
                isNull());
    }

    @Test
    @DisplayName("onCashRegisterDiscrepancy - should notify admins and managers")
    void onCashRegisterDiscrepancy_ShouldNotifyManagers() {
        List<User> managers = List.of(UserFixtures.defaultAdmin(), UserFixtures.defaultManager());
        given(userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.MANAGER)))
                .willReturn(managers);
        CashRegister register = CashRegisterFixtures.closedRegister();
        BigDecimal variance = new BigDecimal("-50.00");

        notificationEventService.onCashRegisterDiscrepancy(register, variance);

        then(notificationService).should().createNotificationForUsers(
                eq(managers),
                eq(NotificationType.CASH_REGISTER_DISCREPANCY),
                argThat(msg -> msg.contains("discrepancy")),
                isNull());
    }

    @Test
    @DisplayName("onSaleCancelled - should notify admins and managers")
    void onSaleCancelled_ShouldNotifyManagers() {
        List<User> managers = List.of(UserFixtures.defaultAdmin(), UserFixtures.defaultManager());
        given(userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.MANAGER)))
                .willReturn(managers);
        Sale sale = SaleFixtures.saleWithDetails();
        sale.setStatus(SaleStatus.CANCELLED);
        sale.setCancellationReason("Customer changed mind");

        notificationEventService.onSaleCancelled(sale);

        then(notificationService).should().createNotificationForUsers(
                eq(managers),
                eq(NotificationType.SALE_CANCELLED),
                argThat(msg -> msg.contains("cancelled") && msg.contains("Customer changed mind")),
                isNull());
    }
}