package com.supermarket.supermarket.unit.service;

import com.supermarket.supermarket.exception.InsufficientStockException;
import com.supermarket.supermarket.model.Branch;
import com.supermarket.supermarket.model.BranchInventory;
import com.supermarket.supermarket.model.Product;
import com.supermarket.supermarket.repository.BranchInventoryRepository;
import com.supermarket.supermarket.service.business.impl.InventoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @Mock
    private BranchInventoryRepository branchInventoryRepository;
    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void validateAndReduceStock_Success() {
        BranchInventory inv = BranchInventory.builder().stock(10).build();
        given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L)).willReturn(Optional.of(inv));
        inventoryService.validateAndReduceStock(1L, 1L, 3);
        assertThat(inv.getStock()).isEqualTo(7);
    }

    @Test
    void validateAndReduceStock_ThrowsException() {
        Product product = Product.builder().id(1L).build();
        Branch branch = Branch.builder().id(1L).build();
        BranchInventory inv = BranchInventory.builder()
                .stock(2)
                .product(product)
                .branch(branch)
                .build();

        given(branchInventoryRepository.findByBranchIdAndProductId(1L, 1L))
                .willReturn(Optional.of(inv));

        assertThatThrownBy(() -> inventoryService.validateAndReduceStock(1L, 1L, 5))
                .isInstanceOf(InsufficientStockException.class);
    }
}