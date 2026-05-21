package com.cb011999.cccp.service.concurrency;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.service.ReportService;
import com.cb011999.cccp.service.impl.ReportServiceImpl;
import com.cb011999.cccp.service.report.model.*;

import java.time.LocalDate;

/**
 * Thread-safe wrapper for ReportService.
 * 
 * Reports are read-only operations so they don't go through the queue.
 * We only synchronize to get consistent reads — preventing a report
 * from reading stock data mid-update by another thread.
 */
public class SynchronizedReportService implements ReportService {

    private final ReportServiceImpl delegate;
    private final Object reportLock = new Object();

    public SynchronizedReportService(ReportServiceImpl delegate) {
        this.delegate = delegate;
    }

    @Override
    public DailySalesReport generateDailySalesReport(LocalDate date,
                                                     TransactionType transactionType,
                                                     StoreType storeType) {
        synchronized (reportLock) {
            return delegate.generateDailySalesReport(date, transactionType, storeType);
        }
    }

    @Override
    public ReshelfReport generateReshelfReport(StoreType storeType, int daysThreshold) {
        synchronized (reportLock) {
            return delegate.generateReshelfReport(storeType, daysThreshold);
        }
    }

    @Override
    public ReorderReport generateReorderReport(StoreType storeType) {
        synchronized (reportLock) {
            return delegate.generateReorderReport(storeType);
        }
    }

    @Override
    public StockReport generateStockReport() {
        synchronized (reportLock) {
            return delegate.generateStockReport();
        }
    }

    @Override
    public BillReport generateBillReport(TransactionType transactionType,
                                         StoreType storeType) {
        synchronized (reportLock) {
            return delegate.generateBillReport(transactionType, storeType);
        }
    }
}