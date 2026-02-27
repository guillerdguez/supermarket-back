package com.supermarket.supermarket.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supermarket.supermarket.controller.NotificationController;
import com.supermarket.supermarket.dto.notification.NotificationResponse;
import com.supermarket.supermarket.exception.GlobalExceptionHandler;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.model.NotificationType;
import com.supermarket.supermarket.service.business.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {
    private MockMvc mockMvc;
    @Mock
    private NotificationService notificationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        NotificationController notificationController = new NotificationController(notificationService);
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private NotificationResponse buildResponse(Long id, boolean read) {
        return NotificationResponse.builder()
                .id(id)
                .userId(1L)
                .username("cashier-test")
                .type(NotificationType.LOW_STOCK)
                .message("Low stock alert for product X")
                .read(read)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /notifications - should return unread list")
    void getUnreadNotifications_ShouldReturnList() throws Exception {
        given(notificationService.getUnreadNotifications())
                .willReturn(List.of(buildResponse(1L, false)));
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    @DisplayName("GET /notifications/all - should return all notifications")
    void getAllNotifications_ShouldReturnList() throws Exception {
        given(notificationService.getAllNotifications())
                .willReturn(List.of(buildResponse(1L, false), buildResponse(2L, true)));
        mockMvc.perform(get("/notifications/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /notifications/count - should return unread count")
    void countUnread_ShouldReturnCount() throws Exception {
        given(notificationService.countUnread()).willReturn(3L);
        mockMvc.perform(get("/notifications/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3));
    }

    @Test
    @DisplayName("PUT /notifications/{id}/read - should mark notification as read")
    void markAsRead_ShouldReturnUpdatedNotification() throws Exception {
        given(notificationService.markAsRead(1L)).willReturn(buildResponse(1L, true));
        mockMvc.perform(put("/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @DisplayName("PUT /notifications/{id}/read - should return 404 when not found")
    void markAsRead_WhenNotFound_ShouldReturn404() throws Exception {
        given(notificationService.markAsRead(99L))
                .willThrow(new ResourceNotFoundException("Notification not found with ID: 99"));
        mockMvc.perform(put("/notifications/99/read"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /notifications/{id} - should return 204")
    void deleteNotification_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/notifications/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /notifications/{id} - should return 404 when not found")
    void deleteNotification_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Notification not found with ID: 99"))
                .when(notificationService).deleteNotification(99L);
        mockMvc.perform(delete("/notifications/99"))
                .andExpect(status().isNotFound());
    }
}