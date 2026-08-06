package com.supermarket.supermarket.fixtures.auth;

import com.supermarket.supermarket.dto.user.UserRequest;
import com.supermarket.supermarket.model.user.UserRole;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthFixtures {
    public static UserRequest userRegisterRequest() {
        return UserRequest.builder()
                .username("test-user")
                .email("user@test.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.CASHIER)
                .build();
    }

    public static UserRequest adminRegisterRequest() {
        return UserRequest.builder()
                .username("admin-test")
                .email("admin@test.com")
                .password("Admin123!")
                .firstName("Admin")
                .lastName("Test")
                .role(UserRole.ADMIN)
                .build();
    }

    public static UserRequest cashierRegisterRequest() {
        return UserRequest.builder()
                .username("cashier-test")
                .email("cashier@test.com")
                .password("Cashier123!")
                .firstName("Cashier")
                .lastName("Test")
                .role(UserRole.CASHIER)
                .build();
    }
}
