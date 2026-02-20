package com.supermarket.supermarket.mapper;

import com.supermarket.supermarket.dto.cashregister.CashRegisterResponse;
import com.supermarket.supermarket.model.CashRegister;
import org.springframework.stereotype.Component;

@Component
public class CashRegisterMapper {
    public CashRegisterResponse toResponse(CashRegister register) {
        if (register == null) return null;
        return CashRegisterResponse.builder()
                .id(register.getId())
                .branchId(register.getBranch() != null ? register.getBranch().getId() : null)
                .branchName(register.getBranch() != null ? register.getBranch().getName() : null)
                .openingBalance(register.getOpeningBalance())
                .closingBalance(register.getClosingBalance())
                .openingTime(register.getOpeningTime())
                .closingTime(register.getClosingTime())
                .status(register.getStatus())
                .openedById(register.getOpenedBy() != null ? register.getOpenedBy().getId() : null)
                .openedByUsername(register.getOpenedBy() != null ? register.getOpenedBy().getUsername() : null)
                .closedById(register.getClosedBy() != null ? register.getClosedBy().getId() : null)
                .closedByUsername(register.getClosedBy() != null ? register.getClosedBy().getUsername() : null)
                .build();
    }
}