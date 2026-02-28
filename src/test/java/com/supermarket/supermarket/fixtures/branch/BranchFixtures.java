package com.supermarket.supermarket.fixtures.branch;

import com.supermarket.supermarket.dto.branch.BranchRequest;
import com.supermarket.supermarket.dto.branch.BranchResponse;
import com.supermarket.supermarket.model.branch.Branch;

public class BranchFixtures {
    public static Branch defaultBranch() {
        return Branch.builder()
                .id(1L)
                .name("Central Branch")
                .address("123 Main St")
                .build();
    }

    public static BranchRequest validBranchRequest() {
        return BranchRequest.builder()
                .name("New Branch")
                .address("456 North Ave")
                .build();
    }

    public static BranchRequest invalidBranchRequest() {
        return BranchRequest.builder()
                .name("")
                .address("")
                .build();
    }

    public static BranchResponse branchResponse() {
        return BranchResponse.builder()
                .id(1L)
                .name("Central Branch")
                .address("123 Main St")
                .build();
    }
}