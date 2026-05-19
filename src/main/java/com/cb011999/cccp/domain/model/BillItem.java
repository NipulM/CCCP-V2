package com.cb011999.cccp.domain.model;

public class BillItem {
    private final String name;
    private final String itemCode;
    private final int quantity;
    private final double unitPrice;
    private final double totalPrice;
    
    public BillItem(String name, String itemCode, int quantity, double unitPrice) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        if (itemCode == null || itemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Item code cannot be null or empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        
        this.name = name;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice * quantity;
    }
    
    // Getters only - immutable object
    public String getName() {
        return name;
    }
    
    public String getItemCode() {
        return itemCode;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) x%d @ %.2f = %.2f", 
            name, itemCode, quantity, unitPrice, totalPrice);
    }
}