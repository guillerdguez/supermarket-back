package com.supermarket.supermarket.service.business;

import com.supermarket.supermarket.model.cashregister.CashRegister;
import com.supermarket.supermarket.model.sale.Sale;
import com.supermarket.supermarket.model.transfer.StockTransfer;

import java.math.BigDecimal;

public interface NotificationEventService {
    void onLowStock(String branchName, String productName, int currentStock, int minStock);

    void onTransferRequested(StockTransfer transfer);

    void onTransferApproved(StockTransfer transfer);

    void onTransferRejected(StockTransfer transfer);

    void onTransferCompleted(StockTransfer transfer);

    void onCashRegisterDiscrepancy(CashRegister register, BigDecimal variance);

    void onSaleCancelled(Sale sale);
}