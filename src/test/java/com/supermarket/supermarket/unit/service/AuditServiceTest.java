package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.dto.audit.AuditLogResponse;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.AuditLogMapper;
import com.supermarket.supermarket.model.audit.AuditLog;
import com.supermarket.supermarket.model.audit.AuditStatus;
import com.supermarket.supermarket.repository.AuditLogRepository;
import com.supermarket.supermarket.service.security.AuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditService auditService;

    @Nested
    @DisplayName("logAction")
    class LogAction {
        @Test
        @DisplayName("should save an audit log with the given username, action and status")
        void logAction_SavesAuditLog() {
            auditService.logAction("admin", "LOGIN_SUCCESS", "logged in", AuditStatus.SUCCESS);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            then(auditLogRepository).should().save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getUsername()).isEqualTo("admin");
            assertThat(saved.getAction()).isEqualTo("LOGIN_SUCCESS");
            assertThat(saved.getDetails()).isEqualTo("logged in");
            assertThat(saved.getStatus()).isEqualTo(AuditStatus.SUCCESS);
            assertThat(saved.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should resolve IP as UNKNOWN when there is no servlet request context")
        void logAction_NoServletContext_IpIsUnknown() {
            auditService.logAction("admin", "LOGIN_SUCCESS", "logged in", AuditStatus.SUCCESS);

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            then(auditLogRepository).should().save(captor.capture());

            assertThat(captor.getValue().getIpAddress()).isEqualTo("UNKNOWN");
        }

        @Test
        @DisplayName("should not propagate exceptions when saving the audit log fails")
        void logAction_SaveFails_DoesNotThrow() {
            given(auditLogRepository.save(any())).willThrow(new RuntimeException("DB down"));

            assertThatCode(() ->
                    auditService.logAction("admin", "LOGIN_FAILED", "bad credentials", AuditStatus.FAILED))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {
        @Test
        @DisplayName("should filter with a specification and map results")
        void getAll_ReturnsMappedPage() {
            AuditLog log = AuditLog.builder()
                    .id(1L).username("admin").action("LOGIN_SUCCESS")
                    .timestamp(LocalDateTime.now()).status(AuditStatus.SUCCESS)
                    .build();
            AuditLogResponse response = AuditLogResponse.builder()
                    .id(1L).username("admin").action("LOGIN_SUCCESS").status(AuditStatus.SUCCESS)
                    .build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<AuditLog> page = new PageImpl<>(List.of(log), pageable, 1);
            given(auditLogRepository.findAll(any(Specification.class), eq(pageable))).willReturn(page);
            given(auditLogMapper.toResponse(log)).willReturn(response);

            Page<AuditLogResponse> result = auditService.getAll(
                    "admin", "LOGIN_SUCCESS", AuditStatus.SUCCESS, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUsername()).isEqualTo("admin");
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {
        @Test
        @DisplayName("should return the mapped audit log when found")
        void getById_Found_ReturnsMapped() {
            AuditLog log = AuditLog.builder().id(1L).username("admin").action("LOGIN_SUCCESS").build();
            AuditLogResponse response = AuditLogResponse.builder().id(1L).username("admin").build();

            given(auditLogRepository.findById(1L)).willReturn(Optional.of(log));
            given(auditLogMapper.toResponse(log)).willReturn(response);

            AuditLogResponse result = auditService.getById(1L);

            assertThat(result.getUsername()).isEqualTo("admin");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void getById_NotFound_Throws() {
            given(auditLogRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> auditService.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }
}
