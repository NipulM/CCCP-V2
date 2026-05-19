package com.cb011999.cccp.service.report.model;

import java.util.ArrayList;
import java.util.List;

public class ReshelfReport {
    private final List<ReshelfItem> items;
    
    public ReshelfReport() {
        this.items = new ArrayList<>();
    }
    
    public void addItem(String itemCode, String itemName, int quantity, String reason) {
        items.add(new ReshelfItem(itemCode, itemName, quantity, reason));
    }
    
    public List<ReshelfItem> getItems() {
        return items;
    }
    
    public int getTotalItems() {
        return items.size();
    }
    
    public int getTotalQuantity() {
        return items.stream().mapToInt(ReshelfItem::getQuantity).sum();
    }
    
    public static class ReshelfItem {
        private final String itemCode;
        private final String itemName;
        private final int quantity;
        private final String reason;
        
        public ReshelfItem(String itemCode, String itemName, int quantity, String reason) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.quantity = quantity;
            this.reason = reason;
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
        
        public String getReason() {
            return reason;
        }
    }
}