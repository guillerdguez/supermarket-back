package com.supermarket.supermarket.integration;

import com.supermarket.supermarket.dto.auth.AuthResponse;
import com.supermarket.supermarket.helper.TestUserHelper;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.model.UserRole;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.repository.UserRepository;
import com.supermarket.supermarket.service.security.RateLimitService;
import com.supermarket.supermarket.service.security.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static com.supermarket.supermarket.fixtures.auth.AuthFixtures.adminRegisterRequest;
import static com.supermarket.supermarket.fixtures.auth.AuthFixtures.cashierRegisterRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PreAuthorizeIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestUserHelper testUserHelper;
    @MockitoBean
    private RateLimitService rateLimitService;
    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;
    private Long testProductId;
    private String adminToken;
    private String cashierToken;

    @BeforeEach
    void setUp() throws Exception {
        saleRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        Product product = Product.builder()
                .name("Test Product")
                .category("Test Category")
                .price(new BigDecimal("10.00"))
                .build();
        product = productRepository.save(product);
        testProductId = product.getId();
        adminToken = testUserHelper.registerAndGetToken(
                adminRegisterRequest(), UserRole.ADMIN);
        cashierToken = testUserHelper.registerAndGetToken(
                cashierRegisterRequest(), UserRole.CASHIER);
    }

    @Test
    @DisplayName("CASHIER role should view products but fail to delete them")
    void cashierCannotDeleteProducts() throws Exception {
        mockMvc.perform(get("/products")
                        .header("Authorization", "Bearer " + cashierToken))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/products/" + testProductId)
                        .header("Authorization", "Bearer " + cashierToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN role should successfully delete products")
    void adminCanDeleteProducts() throws Exception {
        mockMvc.perform(delete("/products/" + testProductId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}