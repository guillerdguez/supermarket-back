package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.cashregister.CashRegisterResponse;
import com.supermarket.supermarket.dto.cashregister.CloseRegisterRequest;
import com.supermarket.supermarket.dto.cashregister.OpenRegisterRequest;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.CashRegisterMapper;
import com.supermarket.supermarket.model.*;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.CashRegisterRepository;
import com.supermarket.supermarket.service.business.CashRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CashRegisterServiceImpl implements CashRegisterService {
    private final CashRegisterRepository cashRegisterRepository;
    private final BranchRepository branchRepository;
    private final CashRegisterMapper cashRegisterMapper;

    @Override
    public CashRegisterResponse openRegister(OpenRegisterRequest request) {
        User currentUser = getCurrentUser();
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        cashRegisterRepository.findByBranchIdAndStatus(branch.getId(), CashRegisterStatus.OPEN)
                .ifPresent(reg -> {
                    throw new InvalidOperationException("There is already an open register for this branch");
                });

        CashRegister register = CashRegister.builder()
                .branch(branch)
                .openingBalance(request.getOpeningBalance())
                .openingTime(LocalDateTime.now())
                .status(CashRegisterStatus.OPEN)
                .openedBy(currentUser)
                .build();

        return cashRegisterMapper.toResponse(cashRegisterRepository.save(register));
    }

    @Override
    public CashRegisterResponse closeRegister(Long registerId, CloseRegisterRequest request) {
        User currentUser = getCurrentUser();
        CashRegister register = cashRegisterRepository.findById(registerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cash register not found"));

        if (register.getStatus() == CashRegisterStatus.CLOSED) {
            throw new InvalidOperationException("Register is already closed");
        }

        register.setClosingBalance(request.getClosingBalance());
        register.setClosingTime(LocalDateTime.now());
        register.setStatus(CashRegisterStatus.CLOSED);
        register.setClosedBy(currentUser);

        return cashRegisterMapper.toResponse(cashRegisterRepository.save(register));
    }

    @Override
    @Transactional(readOnly = true)
    public CashRegisterResponse getCurrentRegisterByBranch(Long branchId) {
        CashRegister register = cashRegisterRepository.findByBranchIdAndStatus(branchId, CashRegisterStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("No open register found for branch " + branchId));
        return cashRegisterMapper.toResponse(register);
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}