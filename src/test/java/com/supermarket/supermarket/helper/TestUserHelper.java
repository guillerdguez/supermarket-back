package com.supermarket.supermarket.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.supermarket.dto.auth.AuthResponse;
import com.supermarket.supermarket.dto.auth.RegisterRequest;
import com.supermarket.supermarket.model.user.UserRole;
import com.supermarket.supermarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class TestUserHelper {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    public String registerAndGetToken(RegisterRequest request, UserRole role) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after registration"));
        user.setRole(role);
        userRepository.save(user);

        String loginJson = String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, request.getEmail(), request.getPassword());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        return authResponse.getToken();
    }
}