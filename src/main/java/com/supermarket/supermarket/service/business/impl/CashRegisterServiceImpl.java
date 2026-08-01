package com.supermarket.supermarket.service.business.impl;

import com.supermarket.supermarket.dto.cashregister.CashRegisterResponse;
import com.supermarket.supermarket.dto.cashregister.CloseRegisterRequest;
import com.supermarket.supermarket.dto.cashregister.OpenRegisterRequest;
import com.supermarket.supermarket.exception.InvalidOperationException;
import com.supermarket.supermarket.exception.ResourceNotFoundException;
import com.supermarket.supermarket.mapper.CashRegisterMapper;
import com.supermarket.supermarket.model.branch.Branch;
import com.supermarket.supermarket.model.cashregister.CashRegister;
import com.supermarket.supermarket.model.cashregister.CashRegisterStatus;
import com.supermarket.supermarket.model.user.User;
import com.supermarket.supermarket.repository.BranchRepository;
import com.supermarket.supermarket.repository.CashRegisterRepository;
import com.supermarket.supermarket.repository.SaleRepository;
import com.supermarket.supermarket.security.SecurityUtils;
import com.supermarket.supermarket.service.business.CashRegisterService;
import com.supermarket.supermarket.service.business.NotificationEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CashRegisterServiceImpl implements CashRegisterService {
    private final CashRegisterRepository cashRegisterRepository;
    private final BranchRepository branchRepository;
    private final CashRegisterMapper cashRegisterMapper;
    private final SecurityUtils securityUtils;
    private final NotificationEventService notificationEventService;
    private final SaleRepository saleRepository;

    @Override
    public CashRegisterResponse openRegister(OpenRegisterRequest request) {
        User currentUser = getCurrentUser();
        Branch branch = resolveBranch(request, currentUser);
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
        CashRegister saved = cashRegisterRepository.save(register);
        notifyIfDiscrepancy(saved);
        return cashRegisterMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CashRegisterResponse getCurrentRegisterByBranch(Long branchId) {
        CashRegister register = cashRegisterRepository.findByBranchIdAndStatus(branchId, CashRegisterStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("No open register found for branch " + branchId));
        return cashRegisterMapper.toResponse(register);
    }

    @Override
    @Transactional(readOnly = true)
    public CashRegister getRegisterEntityByBranch(Long branchId) {
        return cashRegisterRepository.findByBranchIdAndStatus(branchId, CashRegisterStatus.OPEN)
                .orElseThrow(() -> new ResourceNotFoundException("No open register found for branch " + branchId));
    }

    private User getCurrentUser() {
        return securityUtils.getCurrentUser();
    }

    /**
     * Uses the branch sent in the request when present, so ADMIN and MANAGER keep
     * choosing freely. Otherwise falls back to the branch assigned to the user,
     * which is how a cashier opens their own register.
     */
    private Branch resolveBranch(OpenRegisterRequest request, User currentUser) {
        if (request.getBranchId() != null) {
            return branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        }
        if (currentUser.getBranch() == null) {
            throw new InvalidOperationException("This user has no branch assigned");
        }
        return currentUser.getBranch();
    }

    private void notifyIfDiscrepancy(CashRegister register) {
        if (register.getClosingBalance() == null || register.getOpeningBalance() == null) return;
        BigDecimal totalSales = saleRepository.sumTotalByCashRegisterId(register.getId());
        BigDecimal expected = register.getOpeningBalance().add(totalSales);
        BigDecimal variance = register.getClosingBalance().subtract(expected);
        if (variance.abs().compareTo(BigDecimal.ZERO) > 0) {
            try {
                notificationEventService.onCashRegisterDiscrepancy(register, variance);
            } catch (Exception e) {
                log.warn("Failed to send discrepancy notification for register {}: {}",
                        register.getId(), e.getMessage());
            }
        }
    }
}