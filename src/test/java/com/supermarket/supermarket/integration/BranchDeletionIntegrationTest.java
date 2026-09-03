package com.supermarket.supermarket.integration;

import com.supermarket.supermarket.dto.branch.BranchResponse;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.model.branch.Branch;
import com.supermarket.supermarket.model.branch.BranchInventory;
import com.supermarket.supermarket.model.product.Product;
import com.supermarket.supermarket.repository.BranchInventoryRepository;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.ProductRepository;
import com.supermarket.supermarket.service.business.BranchService;
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

import java.math.BigDecimal;
import java.util.List;

import static com.supermarket.supermarket.fixtures.branch.BranchFixtures.validBranchRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BranchDeletionIntegrationTest {

    @Autowired
    private BranchService branchService;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private BranchInventoryRepository branchInventoryRepository;
    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private RateLimitService rateLimitService;
    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        branchInventoryRepository.deleteAll();
        branchRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("DELETE - should delete a branch whose only inventory rows are auto-created placeholders (stock = 0)")
    void delete_branchWithPlaceholderInventoryOnly_shouldSucceed() {
        productRepository.save(Product.builder()
                .name("Test Product")
                .category("Test Category")
                .price(new BigDecimal("10.00"))
                .build());

        BranchResponse created = branchService.create(validBranchRequest());

        List<BranchInventory> placeholders = branchInventoryRepository.findByBranchId(created.getId());
        assertThat(placeholders).hasSize(1);
        assertThat(placeholders.get(0).getStock()).isZero();

        branchService.delete(created.getId());

        assertThat(branchRepository.existsById(created.getId())).isFalse();
        assertThat(branchInventoryRepository.findByBranchId(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE - a branch with stock cannot be deleted, but can be deactivated and keeps its row")
    void delete_branchWithActivity_shouldFailButAllowDeactivation() {
        productRepository.save(Product.builder()
                .name("Test Product")
                .category("Test Category")
                .price(new BigDecimal("10.00"))
                .build());

        BranchResponse created = branchService.create(validBranchRequest());

        BranchInventory inventory = branchInventoryRepository.findByBranchId(created.getId()).get(0);
        inventory.setStock(5);
        branchInventoryRepository.save(inventory);

        assertThatThrownBy(() -> branchService.delete(created.getId()))
                .isInstanceOf(InvalidOperationException.class);

        branchService.deactivate(created.getId());

        Branch persisted = branchRepository.findById(created.getId()).orElseThrow();
        assertThat(persisted.getActive()).isFalse();
        assertThat(branchInventoryRepository.findByBranchId(created.getId())).isNotEmpty();
    }
}
