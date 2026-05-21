package com.cb011999.cccp.service;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.service.report.model.*;

import java.time.LocalDate;

public interface ReportService {

    DailySalesReport generateDailySalesReport(LocalDate date,
                                              TransactionType transactionType,
                                              StoreType storeType);

    ReshelfReport generateReshelfReport(StoreType storeType, int daysThreshold);

    ReorderReport generateReorderReport(StoreType storeType);

    StockReport generateStockReport();

    BillReport generateBillReport(TransactionType transactionType,
                                  StoreType storeType);
}