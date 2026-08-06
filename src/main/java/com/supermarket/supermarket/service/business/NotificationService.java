package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.dto.notification.NotificationResponse;
import com.supermarket.supermarket.model.notification.NotificationType;
import com.supermarket.supermarket.model.notification.ReferenceType;
import com.supermarket.supermarket.model.user.User;

import java.util.List;

public interface NotificationService {
    void createNotification(
            User recipient, NotificationType type, String message, String data,
            ReferenceType referenceType, Long referenceId);

    void createNotificationForUsers(
            List<User> recipients, NotificationType type, String message, String data,
            ReferenceType referenceType, Long referenceId);

    NotificationResponse markAsRead(Long notificationId);

    List<NotificationResponse> getUnreadNotifications();

    List<NotificationResponse> getAllNotifications();

    void deleteNotification(Long notificationId);

    long countUnread();

    int markAllAsRead();
}