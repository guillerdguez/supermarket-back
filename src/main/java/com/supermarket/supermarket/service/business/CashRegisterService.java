package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.dto.cashregister.CashRegisterResponse;
import com.supermarket.supermarket.dto.cashregister.CloseRegisterRequest;
import com.supermarket.supermarket.dto.cashregister.OpenRegisterRequest;

public interface CashRegisterService {
    CashRegisterResponse openRegister(OpenRegisterRequest request);
    CashRegisterResponse closeRegister(Long registerId, CloseRegisterRequest request);
    CashRegisterResponse getCurrentRegisterByBranch(Long branchId);
}