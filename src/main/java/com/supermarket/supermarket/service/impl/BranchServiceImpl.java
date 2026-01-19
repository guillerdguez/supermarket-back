package com.supermarket.supermarket.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.supermarket.supermarket.dto.branch.BranchRequest;
import com.supermarket.supermarket.dto.branch.BranchResponse;
import com.supermarket.supermarket.exception.DuplicateResourceException;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.BranchMapper;
import com.supermarket.supermarket.model.Branch;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.service.BranchService;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepo;
    private final BranchMapper branchMapper;
    private final SaleRepository saleRepo;

    @Transactional(readOnly = true)
    @Override
    public List<BranchResponse> getAll() {
        log.info("Fetching all branches");
        return branchMapper.toResponseList(branchRepo.findAll());
    }

    @Transactional(readOnly = true)
    @Override
    public BranchResponse getById(Long id) {
        log.info("Fetching branch with ID: {}", id);
        return mapToDto(findBranch(id));
    }

    @Override
    public BranchResponse create(BranchRequest request) {
        log.info("Creating new branch: {}", request.getName());

        if (branchRepo.existsByName(request.getName())) {
            throw new DuplicateResourceException("Branch already exists with name: " + request.getName());
        }

        Branch branch = branchMapper.toEntity(request);
        return mapToDto(branchRepo.save(branch));
    }

    @Override
    public BranchResponse update(Long id, BranchRequest request) {
        log.info("Updating branch with ID: {}", id);
        Branch branch = findBranch(id);

        if (request.getName() != null && !request.getName().equals(branch.getName())) {
            if (branchRepo.existsByName(request.getName())) {
                throw new DuplicateResourceException(
                        "Cannot update: Branch name '" + request.getName() + "' is already in use");
            }
        }

        branchMapper.updateEntity(request, branch);
        return mapToDto(branchRepo.save(branch));
    }

    @Override
    public void delete(Long id) {
        log.info("Attempting to delete branch with ID: {}", id);

        Branch branch = branchRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + id));

        if (saleRepo.existsByBranchId(id)) {
            throw new InvalidOperationException("Cannot delete branch: It has associated sales records");
        }

        branchRepo.delete(branch);
        log.info("Branch deleted successfully - ID: {}", id);
    }

    // Auxiliary Methods

    private Branch findBranch(Long id) {
        return branchRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + id));
    }

    private BranchResponse mapToDto(Branch branch) {
        return branchMapper.toResponse(branch);
    }
}