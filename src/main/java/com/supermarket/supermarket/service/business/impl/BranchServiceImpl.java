package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.branch.BranchRequest;
import com.supermarket.supermarket.dto.branch.BranchResponse;
import com.supermarket.supermarket.exception.DuplicateResourceException;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.BranchMapper;
import com.supermarket.supermarket.model.branch.Branch;
import com.supermarket.supermarket.repository.BranchInventoryRepository;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.CashRegisterRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.repository.StockTransferRepository;
import com.supermarket.supermarket.repository.UserRepository;
import com.supermarket.supermarket.service.business.BranchService;
import com.supermarket.supermarket.service.business.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final SaleRepository saleRepo;
    private final BranchInventoryRepository branchInventoryRepo;
    private final CashRegisterRepository cashRegisterRepo;
    private final StockTransferRepository stockTransferRepo;
    private final UserRepository userRepo;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    @Override
    public List<BranchResponse> getAll(boolean includeInactive) {
        log.info("Fetching branches - includeInactive: {}", includeInactive);
        List<Branch> branches = includeInactive
                ? branchRepository.findAll()
                : branchRepository.findAllByActiveTrue();
        return branchMapper.toResponseList(branches);
    }

    @Transactional(readOnly = true)
    @Override
    public BranchResponse getById(Long id) {
        log.info("Fetching branch with ID: {}", id);
        return branchMapper.toResponse(findBranch(id));
    }

    @Override
    public BranchResponse create(BranchRequest request) {
        log.info("Creating new branch: {}", request.getName());
        if (branchRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Branch already exists with name: " + request.getName());
        }
        Branch branch = branchMapper.toEntity(request);
        Branch saved = branchRepository.save(branch);
        inventoryService.initializeInventoryForNewBranch(saved);
        return branchMapper.toResponse(saved);
    }

    @Override
    public BranchResponse update(Long id, BranchRequest request) {
        log.info("Updating branch with ID: {}", id);
        Branch branch = findBranch(id);

        if (request.getName() != null && !request.getName().equals(branch.getName())) {
            if (branchRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException(
                        "Cannot update: Branch name '" + request.getName() + "' is already in use");
            }
        }

        branchMapper.updateEntity(request, branch);
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    public void deactivate(Long id) {
        log.info("Deactivating branch with ID: {}", id);
        Branch branch = findBranch(id);
        branch.setActive(false);
        branchRepository.save(branch);
        log.info("Branch deactivated successfully - ID: {}", id);
    }

    @Override
    public void reactivate(Long id) {
        log.info("Reactivating branch with ID: {}", id);
        Branch branch = findBranch(id);
        branch.setActive(true);
        branchRepository.save(branch);
        log.info("Branch reactivated successfully - ID: {}", id);
    }

    @Override
    public void delete(Long id) {
        log.info("Attempting to delete branch with ID: {}", id);

        Branch branch = findBranch(id);

        if (saleRepo.existsByBranchId(id)) {
            throw new InvalidOperationException(
                    "Cannot delete branch: It has associated sales records. Deactivate it instead");
        }
        if (cashRegisterRepo.existsByBranchId(id)) {
            throw new InvalidOperationException(
                    "Cannot delete branch: It has associated cash registers. Deactivate it instead");
        }
        if (stockTransferRepo.existsBySourceBranchIdOrTargetBranchId(id, id)) {
            throw new InvalidOperationException(
                    "Cannot delete branch: It has associated stock transfers. Deactivate it instead");
        }
        if (userRepo.existsByBranchId(id)) {
            throw new InvalidOperationException(
                    "Cannot delete branch: It has users assigned to it. Deactivate it instead");
        }
        if (branchInventoryRepo.existsByBranchIdAndStockGreaterThan(id, 0)) {
            throw new InvalidOperationException(
                    "Cannot delete branch: It has products in stock. Deactivate it instead");
        }

        branchInventoryRepo.deleteByBranchId(id);
        branchRepository.delete(branch);
        log.info("Branch deleted successfully - ID: {}", id);
    }

    private Branch findBranch(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with ID: " + id));
    }

}
