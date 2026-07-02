package com.supermarket.supermarket.mapper;

import com.supermarket.supermarket.dto.inventory.BranchInventoryResponse;
import com.supermarket.supermarket.model.branch.BranchInventory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BranchInventoryMapper {

    public BranchInventoryResponse toResponse(BranchInventory inventory) {
        if (inventory == null) {
            return null;
        }

        return BranchInventoryResponse.builder()
                .id(inventory.getId())
                .branchId(inventory.getBranch() != null ? inventory.getBranch().getId() : null)
                .branchName(inventory.getBranch() != null ? inventory.getBranch().getName() : null)
                .productId(inventory.getProduct() != null ? inventory.getProduct().getId() : null)
                .productName(inventory.getProduct() != null ? inventory.getProduct().getName() : null)
                .productCategory(inventory.getProduct() != null ? inventory.getProduct().getCategory() : null)
                .stock(inventory.getStock())
                .minStock(inventory.getMinStock())
                .lastRestockDate(inventory.getLastRestockDate())
                .build();
    }

    public List<BranchInventoryResponse> toResponseList(List<BranchInventory> inventories) {
        if (inventories == null) {
            return null;
        }
        return inventories.stream()
                .map(this::toResponse)
                .toList();
    }
}