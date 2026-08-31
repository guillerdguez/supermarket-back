package com.supermarket.supermarket.fixtures.cashregister;

import com.supermarket.supermarket.dto.cashregister.CashRegisterResponse;
import com.supermarket.supermarket.dto.cashregister.CloseRegisterRequest;
import com.supermarket.supermarket.dto.cashregister.OpenRegisterRequest;
import com.supermarket.supermarket.fixtures.branch.BranchFixtures;
import com.supermarket.supermarket.fixtures.user.UserFixtures;
import com.supermarket.supermarket.model.branch.Branch;
import com.supermarket.supermarket.model.cashregister.CashRegister;
import com.supermarket.supermarket.model.cashregister.CashRegisterStatus;
import com.supermarket.supermarket.model.user.User;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@UtilityClass
public class CashRegisterFixtures {

    public static CashRegister openRegister() {
        return openRegister(1L, BranchFixtures.defaultBranch(), UserFixtures.defaultCashier());
    }

    public static CashRegister openRegister(Long id, Branch branch, User openedBy) {
        return CashRegister.builder()
                .id(id)
                .branch(branch)
                .openingBalance(new BigDecimal("100.00"))
                .openingTime(LocalDateTime.now())
                .status(CashRegisterStatus.OPEN)
                .openedBy(openedBy)
                .build();
    }

    public static CashRegister closedRegister() {
        return closedRegister(1L, BranchFixtures.defaultBranch(),
                UserFixtures.defaultCashier(), UserFixtures.defaultManager());
    }

    public static CashRegister closedRegister(Long id, Branch branch, User openedBy, User closedBy) {
        return CashRegister.builder()
                .id(id)
                .branch(branch)
                .openingBalance(new BigDecimal("100.00"))
                .closingBalance(new BigDecimal("150.00"))
                .openingTime(LocalDateTime.now().minusHours(8))
                .closingTime(LocalDateTime.now())
                .status(CashRegisterStatus.CLOSED)
                .openedBy(openedBy)
                .closedBy(closedBy)
                .build();
    }

    public static OpenRegisterRequest validOpenRegisterRequest() {
        return OpenRegisterRequest.builder()
                .branchId(1L)
                .openingBalance(new BigDecimal("100.00"))
                .build();
    }

    public static CloseRegisterRequest validCloseRegisterRequest() {
        return CloseRegisterRequest.builder()
                .closingBalance(new BigDecimal("150.00"))
                .build();
    }

    public static CashRegisterResponse openRegisterResponse() {
        return CashRegisterResponse.builder()
                .id(1L)
                .branchId(1L)
                .branchName("Central Warehouse")
                .openingBalance(new BigDecimal("100.00"))
                .openingTime(LocalDateTime.now())
                .status(CashRegisterStatus.OPEN)
                .openedById(1L)
                .openedByUsername("cashier-test")
                .build();
    }

    public static CashRegisterResponse closedRegisterResponse() {
        return CashRegisterResponse.builder()
                .id(1L)
                .branchId(1L)
                .branchName("Central Warehouse")
                .openingBalance(new BigDecimal("100.00"))
                .closingBalance(new BigDecimal("150.00"))
                .openingTime(LocalDateTime.now().minusHours(8))
                .closingTime(LocalDateTime.now())
                .status(CashRegisterStatus.CLOSED)
                .openedById(1L)
                .openedByUsername("cashier-test")
                .closedById(2L)
                .closedByUsername("manager-test")
                .build();
    }
}