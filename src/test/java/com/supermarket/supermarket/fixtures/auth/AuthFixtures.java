package com.supermarket.supermarket.fixtures.auth;

import com.supermarket.supermarket.dto.auth.RegisterRequest;

public class AuthFixtures {
    public static RegisterRequest userRegisterRequest() {
        return RegisterRequest.builder()
                .username("test-user")
                .email("user@test.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    public static RegisterRequest adminRegisterRequest() {
        return RegisterRequest.builder()
                .username("admin-test")
                .email("admin@test.com")
                .password("Admin123!")
                .firstName("Admin")
                .lastName("Test")
                .build();
    }

    public static RegisterRequest cashierRegisterRequest() {
        return RegisterRequest.builder()
                .username("cashier-test")
                .email("cashier@test.com")
                .password("Cashier123!")
                .firstName("Cashier")
                .lastName("Test")
                .build();
    }
}