package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.dto.branch.BranchRequest;
import com.supermarket.supermarket.dto.branch.BranchResponse;

import java.util.List;

public interface BranchService {
    List<BranchResponse> getAll(boolean includeInactive);

    BranchResponse getById(Long id);

    BranchResponse create(BranchRequest branch);

    BranchResponse update(Long id, BranchRequest branch);

    void deactivate(Long id);

    void reactivate(Long id);

    void delete(Long id);

}