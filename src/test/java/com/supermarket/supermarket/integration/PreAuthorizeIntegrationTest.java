package com.supermarket.supermarket.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.supermarket.dto.auth.AuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql") // Necesitas crear este archivo
class PreAuthorizeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String obtainToken(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);
        return authResponse.getToken();
    }

    @Test
    @DisplayName("CASHIER can view but not delete products")
    void cashierCannotDeleteProducts() throws Exception {
        String token = obtainToken("cashier@supermarket.com", "Cashier123!");

        mockMvc.perform(get("/products")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/products/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN can delete products")
    void adminCanDeleteProducts() throws Exception {
        String token = obtainToken("admin@supermarket.com", "Admin123!");

        mockMvc.perform(delete("/products/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}