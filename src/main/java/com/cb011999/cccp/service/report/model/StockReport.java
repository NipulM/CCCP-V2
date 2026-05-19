package com.cb011999.cccp.service.report.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockReport {
    private final List<StockBatchInfo> batches;
    
    public StockReport() {
        this.batches = new ArrayList<>();
    }
    
    public void addBatch(String itemCode, String itemName, int quantity, 
                        LocalDate purchaseDate, LocalDate expiryDate) {
        batches.add(new StockBatchInfo(itemCode, itemName, quantity, purchaseDate, expiryDate));
    }
    
    public List<StockBatchInfo> getBatches() {
        return batches;
    }
    
    public int getTotalBatches() {
        return batches.size();
    }
    
    public int getTotalQuantity() {
        return batches.stream().mapToInt(StockBatchInfo::getQuantity).sum();
    }
    
    public static class StockBatchInfo {
        private final String itemCode;
        private final String itemName;
        private final int quantity;
        private final LocalDate purchaseDate;
        private final LocalDate expiryDate;
        
        public StockBatchInfo(String itemCode, String itemName, int quantity, 
                             LocalDate purchaseDate, LocalDate expiryDate) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.quantity = quantity;
            this.purchaseDate = purchaseDate;
            this.expiryDate = expiryDate;
        }
        
        public String getItemCode() {
            return itemCode;
        }
        
        public String getItemName() {
            return itemName;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public LocalDate getPurchaseDate() {
            return purchaseDate;
        }
        
        public LocalDate getExpiryDate() {
            return expiryDate;
        }
        
        public long getDaysUntilExpiry() {
            return LocalDate.now().until(expiryDate).getDays();
        }
    }
}