package com.cb011999.cccp.service.report.model;

import java.util.ArrayList;
import java.util.List;


public class ReorderReport {
    private static final int REORDER_THRESHOLD = 50;
    private final List<ReorderItem> items;
    
    public ReorderReport() {
        this.items = new ArrayList<>();
    }
    
    public void addItem(String itemCode, String itemName, int currentQuantity, int suggestedOrder) {
        items.add(new ReorderItem(itemCode, itemName, currentQuantity, suggestedOrder));
    }
    
    public List<ReorderItem> getItems() {
        return items;
    }
    
    public int getTotalItemsNeedingReorder() {
        return items.size();
    }
    
    public static int getReorderThreshold() {
        return REORDER_THRESHOLD;
    }
    
    public static class ReorderItem {
        private final String itemCode;
        private final String itemName;
        private final int currentQuantity;
        private final int suggestedOrderQuantity;
        
        public ReorderItem(String itemCode, String itemName, int currentQuantity, 
                          int suggestedOrderQuantity) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.currentQuantity = currentQuantity;
            this.suggestedOrderQuantity = suggestedOrderQuantity;
        }
        
        public String getItemCode() {
            return itemCode;
        }
        
        public String getItemName() {
            return itemName;
        }
        
        public int getCurrentQuantity() {
            return currentQuantity;
        }
        
        public int getSuggestedOrderQuantity() {
            return suggestedOrderQuantity;
        }
    }
}