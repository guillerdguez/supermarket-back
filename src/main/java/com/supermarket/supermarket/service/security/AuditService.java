package com.supermarket.supermarket.service.security;

import com.supermarket.supermarket.dto.audit.AuditLogResponse;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.AuditLogMapper;
import com.supermarket.supermarket.model.audit.AuditLog;
import com.supermarket.supermarket.model.audit.AuditStatus;
import com.supermarket.supermarket.repository.AuditLogRepository;
import com.supermarket.supermarket.specification.AuditLogSpecifications;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public void logAction(String username, String action, String details, AuditStatus status) {
        try {
            String ipAddress = getClientIpAddress();
            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .action(action)
                    .details(details)
                    .ipAddress(ipAddress)
                    .timestamp(LocalDateTime.now())
                    .status(status)
                    .build();
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} by {} from {}", action, username, ipAddress);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAll(
            String username,
            String action,
            AuditStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        Specification<AuditLog> spec =
                AuditLogSpecifications.withFilters(username, action, status, fromDate, toDate);
        return auditLogRepository.findAll(spec, pageable).map(auditLogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getById(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found with ID: " + id));
        return auditLogMapper.toResponse(auditLog);
    }

    private String getClientIpAddress() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "UNKNOWN";
    }
}