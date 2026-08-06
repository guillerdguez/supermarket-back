package com.supermarket.supermarket.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.supermarket.dto.auth.AuthResponse;
import com.supermarket.supermarket.dto.user.UserRequest;
import com.supermarket.supermarket.model.user.User;
import com.supermarket.supermarket.model.user.UserRole;
import com.supermarket.supermarket.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    // There's no public self-registration endpoint (users are always created by an ADMIN,
    // already active), so tests create the user directly through the repository instead of
    // going through HTTP, then log in for a real token.
    public String registerAndGetToken(UserRequest request, UserRole role) throws Exception {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(role)
                .active(true)
                .build();
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
