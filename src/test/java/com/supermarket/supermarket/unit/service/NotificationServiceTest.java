package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.dto.notification.NotificationResponse;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.fixtures.user.UserFixtures;
import com.supermarket.supermarket.mapper.NotificationMapper;
import com.supermarket.supermarket.model.Notification;
import com.supermarket.supermarket.model.NotificationType;
import com.supermarket.supermarket.model.User;
import com.supermarket.supermarket.repository.NotificationRepository;
import com.supermarket.supermarket.security.SecurityUtils;
import com.supermarket.supermarket.service.business.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private SecurityUtils securityUtils;
    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = UserFixtures.defaultCashier();
    }

    @Test
    @DisplayName("createNotification - should save notification for given user")
    void createNotification_ShouldSaveNotification() {
        User recipient = UserFixtures.defaultManager();
        notificationService.createNotification(recipient, NotificationType.LOW_STOCK, "Low stock alert", null);
        then(notificationRepository).should().save(argThat(n ->
                n.getUser().equals(recipient) &&
                        n.getType() == NotificationType.LOW_STOCK &&
                        n.getMessage().equals("Low stock alert") &&
                        !n.getRead()
        ));
    }

    @Test
    @DisplayName("createNotificationForUsers - should save one notification per user")
    void createNotificationForUsers_ShouldSaveAll() {
        List<User> users = List.of(UserFixtures.defaultAdmin(), UserFixtures.defaultManager());
        notificationService.createNotificationForUsers(users, NotificationType.SALE_CANCELLED, "Sale cancelled", null);
        then(notificationRepository).should().saveAll(argThat(list -> {
            List<?> items = (List<?>) list;
            return items.size() == 2;
        }));
    }

    @Test
    @DisplayName("createNotificationForUsers - should do nothing when list is empty")
    void createNotificationForUsers_EmptyList_ShouldDoNothing() {
        notificationService.createNotificationForUsers(List.of(), NotificationType.LOW_STOCK, "msg", null);
        then(notificationRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("markAsRead - should mark notification as read")
    void markAsRead_ShouldSetReadTrue() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);
        Notification notification = Notification.builder()
                .id(1L).user(mockUser).type(NotificationType.LOW_STOCK)
                .message("Low stock").read(false).build();
        NotificationResponse response = NotificationResponse.builder().id(1L).read(true).build();
        given(notificationRepository.findByIdAndUserId(1L, mockUser.getId()))
                .willReturn(Optional.of(notification));
        given(notificationRepository.save(notification)).willReturn(notification);
        given(notificationMapper.toResponse(notification)).willReturn(response);
        NotificationResponse result = notificationService.markAsRead(1L);
        assertThat(result.getRead()).isTrue();
        assertThat(notification.getRead()).isTrue();
        then(notificationRepository).should().save(notification);
    }

    @Test
    @DisplayName("markAsRead - should throw when notification not found or not owned")
    void markAsRead_WhenNotFound_ShouldThrow() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);
        given(notificationRepository.findByIdAndUserId(99L, mockUser.getId()))
                .willReturn(Optional.empty());
        assertThatThrownBy(() -> notificationService.markAsRead(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getUnreadNotifications - should return only unread for current user")
    void getUnreadNotifications_ShouldReturnUnread() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);
        Notification notification = Notification.builder()
                .id(1L).user(mockUser).type(NotificationType.LOW_STOCK)
                .message("Low stock").read(false).build();
        NotificationResponse response = NotificationResponse.builder().id(1L).read(false).build();
        given(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(mockUser.getId()))
                .willReturn(List.of(notification));
        given(notificationMapper.toResponseList(List.of(notification))).willReturn(List.of(response));
        List<NotificationResponse> result = notificationService.getUnreadNotifications();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRead()).isFalse();
    }

    @Test
    @DisplayName("getAllNotifications - should return all notifications for current user")
    void getAllNotifications_ShouldReturnAll() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);
        Notification n1 = Notification.builder().id(1L).user(mockUser)
                .type(NotificationType.LOW_STOCK).message("msg1").read(false).build();
        Notification n2 = Notification.builder().id(2L).user(mockUser)
                .type(NotificationType.SALE_CANCELLED).message("msg2").read(true).build();
        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(mockUser.getId()))
                .willReturn(List.of(n1, n2));
        given(notificationMapper.toResponseList(List.of(n1, n2)))
                .willReturn(List.of(
                        NotificationResponse.builder().id(1L).read(false).build(),
                        NotificationResponse.builder().id(2L).read(true).build()));
        List<NotificationResponse> result = notificationService.getAllNotifications();
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("deleteNotification - should delete when owned by current user")
    void deleteNotification_WhenOwned_ShouldDelete() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);
        Notification notification = Notification.builder()
                .id(1L).user(mockUser).type(NotificationType.LOW_STOCK)
                .message("msg").read(false).build();
        given(notificationRepository.findByIdAndUserId(1L, mockUser.getId()))
                .willReturn(Optional.of(notification));
        notificationService.deleteNotification(1L);
        then(notificationRepository).should().delete(notification);
    }

    @Test
    @DisplayName("deleteNotification - should throw when not found or not owned")
    void deleteNotification_WhenNotFound_ShouldThrow() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);
        given(notificationRepository.findByIdAndUserId(99L, mockUser.getId()))
                .willReturn(Optional.empty());
        assertThatThrownBy(() -> notificationService.deleteNotification(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        then(notificationRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("countUnread - should return count for current user")
    void countUnread_ShouldReturnCount() {
        given(securityUtils.getCurrentUser()).willReturn(mockUser);
        given(notificationRepository.countByUserIdAndReadFalse(mockUser.getId())).willReturn(5L);
        long count = notificationService.countUnread();
        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("deleteOldNotifications - should delete notifications older than 30 days")
    void deleteOldNotifications_ShouldInvokeRepository() {
        given(notificationRepository.deleteOlderThan(any(LocalDateTime.class))).willReturn(12);
        notificationService.deleteOldNotifications();
        then(notificationRepository).should().deleteOlderThan(any(LocalDateTime.class));
    }
}