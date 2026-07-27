package com.supermarket.supermarket.specification;

import com.supermarket.supermarket.model.audit.AuditLog;
import com.supermarket.supermarket.model.audit.AuditStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecifications {

    public static Specification<AuditLog> withFilters(
            String username,
            String action,
            AuditStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(username)) {
                predicates.add(cb.like(cb.lower(root.get("username")),
                        "%" + username.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(action)) {
                predicates.add(cb.like(cb.lower(root.get("action")),
                        "%" + action.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), toDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}