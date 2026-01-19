package com.supermarket.supermarket.mapper;

import org.springframework.stereotype.Component;

import com.supermarket.supermarket.dto.branch.BranchRequest;
import com.supermarket.supermarket.dto.branch.BranchResponse;
import com.supermarket.supermarket.model.Branch;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BranchMapper {

    public BranchResponse toResponse(Branch branch) {
        if (branch == null)
            return null;

        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .build();
    }

    public Branch toEntity(BranchRequest request) {
        if (request == null)
            return null;

        return Branch.builder()
                .name(request.getName())
                .address(request.getAddress())
                .build();
    }

    public void updateEntity(BranchRequest request, Branch target) {
        if (request == null || target == null)
            return;

        if (request.getName() != null) {
            target.setName(request.getName());
        }

        if (request.getAddress() != null) {
            target.setAddress(request.getAddress());
        }

    }

    public List<BranchResponse> toResponseList(List<Branch> branches) {
        if (branches == null)
            return null;

        return branches.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}