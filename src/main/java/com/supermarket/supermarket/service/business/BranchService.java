package com.supermarket.supermarket.service.business;

import java.util.List;

import com.supermarket.supermarket.dto.branch.BranchRequest;
import com.supermarket.supermarket.dto.branch.BranchResponse;

public interface BranchService {
    List<BranchResponse> getAll();

    BranchResponse getById(Long id);

    BranchResponse create(BranchRequest branch);

    BranchResponse update(Long id, BranchRequest branch);

    void delete(Long id);
}