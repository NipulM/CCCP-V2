package com.cb011999.cccp.service.report.model;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BillReport {
    private final List<BillSummary> bills;
    
    public BillReport() {
        this.bills = new ArrayList<>();
    }
    
    public void addBill(int serialNumber, LocalDateTime billDate, double totalAmount, 
                       double discount, double finalAmount, TransactionType transactionType,
                       StoreType storeType, int itemCount) {
        bills.add(new BillSummary(serialNumber, billDate, totalAmount, discount, 
                                 finalAmount, transactionType, storeType, itemCount));
    }
    
    public List<BillSummary> getBills() {
        return bills;
    }
    
    public int getTotalBills() {
        return bills.size();
    }
    
    public double getTotalRevenue() {
        return bills.stream().mapToDouble(BillSummary::getFinalAmount).sum();
    }
    
    public static class BillSummary {
        private final int serialNumber;
        private final LocalDateTime billDate;
        private final double totalAmount;
        private final double discount;
        private final double finalAmount;
        private final TransactionType transactionType;
        private final StoreType storeType;
        private final int itemCount;
        
        public BillSummary(int serialNumber, LocalDateTime billDate, double totalAmount,
                          double discount, double finalAmount, TransactionType transactionType,
                          StoreType storeType, int itemCount) {
            this.serialNumber = serialNumber;
            this.billDate = billDate;
            this.totalAmount = totalAmount;
            this.discount = discount;
            this.finalAmount = finalAmount;
            this.transactionType = transactionType;
            this.storeType = storeType;
            this.itemCount = itemCount;
        }
        
        public int getSerialNumber() {
            return serialNumber;
        }
        
        public LocalDateTime getBillDate() {
            return billDate;
        }
        
        public double getTotalAmount() {
            return totalAmount;
        }
        
        public double getDiscount() {
            return discount;
        }
        
        public double getFinalAmount() {
            return finalAmount;
        }
        
        public TransactionType getTransactionType() {
            return transactionType;
        }
        
        public StoreType getStoreType() {
            return storeType;
        }
        
        public int getItemCount() {
            return itemCount;
        }
    }
}