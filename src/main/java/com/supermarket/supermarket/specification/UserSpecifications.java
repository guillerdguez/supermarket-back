package com.supermarket.supermarket.specification;

import com.supermarket.supermarket.model.user.User;
import com.supermarket.supermarket.model.user.UserRole;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    public static Specification<User> withFilters(String username, String email, UserRole role) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(username)) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(email)) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            if (role != null) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}