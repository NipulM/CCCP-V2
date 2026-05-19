package com.cb011999.cccp.domain.model;


public class Item {
    private final String itemCode;
    private String name;
    private double unitPrice;
    private Category category;

    public Item(String itemCode, String name, double unitPrice, Category category) {
        if (itemCode == null || itemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Item code cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        
        this.itemCode = itemCode;
        this.name = name;
        this.unitPrice = unitPrice;
        this.category = category;
    }
    
    // Getters
    public String getItemCode() {
        return itemCode;
    }
    
    public String getName() {
        return name;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public Category getCategory() {
        return category;
    }
    
    // Setters for mutable properties
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        this.name = name;
    }
    
    public void setUnitPrice(double unitPrice) {
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.unitPrice = unitPrice;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s (%.2f)", itemCode, name, unitPrice);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Item item = (Item) obj;
        return itemCode.equals(item.itemCode);
    }
    
    @Override
    public int hashCode() {
        return itemCode.hashCode();
    }
}