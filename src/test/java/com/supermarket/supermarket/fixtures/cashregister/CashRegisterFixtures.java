package com.supermarket.supermarket.fixtures.cashregister;

import com.supermarket.supermarket.fixtures.branch.BranchFixtures;
import com.supermarket.supermarket.fixtures.user.UserFixtures;
import com.supermarket.supermarket.model.branch.Branch;
import com.supermarket.supermarket.model.cashregister.CashRegister;
import com.supermarket.supermarket.model.cashregister.CashRegisterStatus;
import com.supermarket.supermarket.model.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}