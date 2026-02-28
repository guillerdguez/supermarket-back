package com.supermarket.supermarket.mapper;

import com.supermarket.supermarket.dto.notification.NotificationResponse;
import com.supermarket.supermarket.model.notification.Notification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {
    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) return null;
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser() != null ? notification.getUser().getId() : null)
                .username(notification.getUser() != null ? notification.getUser().getUsername() : null)
                .type(notification.getType())
                .message(notification.getMessage())
                .data(notification.getData())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        if (notifications == null) return null;
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}