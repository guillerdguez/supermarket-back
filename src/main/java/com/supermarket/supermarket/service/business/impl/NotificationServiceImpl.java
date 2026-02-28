package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.notification.NotificationResponse;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.NotificationMapper;
import com.supermarket.supermarket.model.notification.Notification;
import com.supermarket.supermarket.model.notification.NotificationType;
import com.supermarket.supermarket.model.user.User;
import com.supermarket.supermarket.repository.NotificationRepository;
import com.supermarket.supermarket.security.SecurityUtils;
import com.supermarket.supermarket.service.business.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SecurityUtils securityUtils;

    @Override
    public void createNotification(User recipient, NotificationType type, String message, String data) {
        Notification notification = Notification.builder()
                .user(recipient)
                .type(type)
                .message(message)
                .data(data)
                .read(false)
                .build();
        notificationRepository.save(notification);
        log.info("Notification created for user {} - type: {}", recipient.getEmail(), type);
    }

    @Override
    public void createNotificationForUsers(List<User> recipients, NotificationType type, String message, String data) {
        if (recipients == null || recipients.isEmpty()) return;
        List<Notification> notifications = recipients.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .type(type)
                        .message(message)
                        .data(data)
                        .read(false)
                        .build())
                .collect(Collectors.toList());
        notificationRepository.saveAll(notifications);
        log.info("Notification created for {} users - type: {}", recipients.size(), type);
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId) {
        User currentUser = securityUtils.getCurrentUser();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        notification.setRead(true);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications() {
        User currentUser = securityUtils.getCurrentUser();
        return notificationMapper.toResponseList(
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(currentUser.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        User currentUser = securityUtils.getCurrentUser();
        return notificationMapper.toResponseList(
                notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()));
    }

    @Override
    public void deleteNotification(Long notificationId) {
        User currentUser = securityUtils.getCurrentUser();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        notificationRepository.delete(notification);
        log.info("Notification {} deleted by user {}", notificationId, currentUser.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread() {
        User currentUser = securityUtils.getCurrentUser();
        return notificationRepository.countByUserIdAndReadFalse(currentUser.getId());
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        int deleted = notificationRepository.deleteOlderThan(cutoff);
        log.info("Scheduled cleanup: deleted {} notifications older than 30 days", deleted);
    }
}