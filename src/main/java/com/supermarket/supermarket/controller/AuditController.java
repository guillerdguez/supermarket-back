package com.supermarket.supermarket.controller;

import com.supermarket.supermarket.dto.audit.AuditLogResponse;
import com.supermarket.supermarket.model.audit.AuditStatus;
import com.supermarket.supermarket.service.security.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Endpoints for audit log management - Admin only")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "List audit logs with filters")
    public ResponseEntity<List<AuditLogResponse>> getAll(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) AuditStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(
                auditService.getAll(username, action, status, fromDate, toDate));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an audit log by ID")
    public ResponseEntity<AuditLogResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getById(id));
    }
}