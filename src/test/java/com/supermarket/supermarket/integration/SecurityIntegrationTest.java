package com.supermarket.supermarket.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.supermarket.config.TestRedisConfig;
import com.supermarket.supermarket.dto.auth.AuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(TestRedisConfig.class)
class SecurityIntegrationTest {

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Access to protected endpoint without token should return 401")
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/branches"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Full authentication flow should return 403 for unauthorized resource access")
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

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

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