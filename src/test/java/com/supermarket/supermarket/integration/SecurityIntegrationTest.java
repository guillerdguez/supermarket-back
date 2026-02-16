package com.supermarket.supermarket.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.supermarket.dto.auth.AuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Acceso a endpoint protegido sin token → 401")
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/branches"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Registro → Login → Endpoint protegido → 403 (autenticación correcta, rol insuficiente)")
    void shouldAuthenticateAndAccessProtectedEndpoint() throws Exception {
        String registerJson = """
            {
                "username": "test-integration",
                "email": "test@integration.com",
                "password": "Password123!",
                "firstName": "Test",
                "lastName": "Integration"
            }
        """;

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andReturn();

        String loginJson = """
            {
                "email": "test@integration.com",
                "password": "Password123!"
            }
        """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        String token = authResponse.getToken();

        mockMvc.perform(get("/branches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}