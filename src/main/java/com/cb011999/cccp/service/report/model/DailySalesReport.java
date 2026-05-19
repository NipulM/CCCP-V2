package com.cb011999.cccp.service.report.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailySalesReport {
    private final LocalDate reportDate;
    private final List<SalesItem> items;
    private double totalRevenue;
    
    public DailySalesReport(LocalDate reportDate) {
        this.reportDate = reportDate;
        this.items = new ArrayList<>();
        this.totalRevenue = 0.0;
    }
    
    public void addItem(String itemCode, String itemName, int quantity, double revenue) {
        items.add(new SalesItem(itemCode, itemName, quantity, revenue));
        this.totalRevenue += revenue;
    }
    
    public LocalDate getReportDate() {
        return reportDate;
    }
    
    public List<SalesItem> getItems() {
        return items;
    }
    
    public double getTotalRevenue() {
        return totalRevenue;
    }
    
    public static class SalesItem {
        private final String itemCode;
        private final String itemName;
        private final int totalQuantity;
        private final double totalRevenue;
        
        public SalesItem(String itemCode, String itemName, int totalQuantity, double totalRevenue) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.totalQuantity = totalQuantity;
            this.totalRevenue = totalRevenue;
        }
        
        public String getItemCode() {
            return itemCode;
        }
        
        public String getItemName() {
            return itemName;
        }
        
        public int getTotalQuantity() {
            return totalQuantity;
        }
        
        public double getTotalRevenue() {
            return totalRevenue;
        }
    }
}