package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.dto.notification.NotificationResponse;
import com.supermarket.supermarket.model.NotificationType;
import com.supermarket.supermarket.model.User;

import java.util.List;

public interface NotificationService {
    void createNotification(User recipient, NotificationType type, String message, String data);

    void createNotificationForUsers(List<User> recipients, NotificationType type, String message, String data);

    NotificationResponse markAsRead(Long notificationId);

    List<NotificationResponse> getUnreadNotifications();

    List<NotificationResponse> getAllNotifications();

    void deleteNotification(Long notificationId);

    long countUnread();
}