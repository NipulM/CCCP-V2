package com.cb011999.cccp.domain.model;

import java.time.LocalDate;

public class StockBatch {
    /** Database row id when loaded from DB; null when created in app. Used for reliable UPDATE. */
    private Integer id;
    private final String itemCode;
    private final LocalDate purchaseDate;
    private final LocalDate expiryDate;
    private int quantity;
    

    public StockBatch(String itemCode, int quantity, LocalDate purchaseDate, LocalDate expiryDate) {
        if (itemCode == null || itemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Item code cannot be null or empty");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (purchaseDate == null) {
            throw new IllegalArgumentException("Purchase date cannot be null");
        }
        if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date cannot be null");
        }
        if (expiryDate.isBefore(purchaseDate)) {
            throw new IllegalArgumentException("Expiry date cannot be before purchase date");
        }
        
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
    }
    
    public boolean hasStock() {
        return quantity > 0;
    }
    
    public void reduceStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (amount > quantity) {
            throw new IllegalArgumentException(
                String.format("Cannot reduce stock by %d. Only %d available.", amount, quantity)
            );
        }
        this.quantity -= amount;
    }
    
    public void addStock(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.quantity += amount;
    }
    
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public long getDaysUntilExpiry() {
        return LocalDate.now().until(expiryDate).getDays();
    }
    
    // Getters
    public String getItemCode() {
        return itemCode;
    }
    
    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public int getQuantity() {
        return quantity;
    }

    /** For repository loading only: set DB row id so updates use WHERE id = ? */
    public void setId(int id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return String.format("StockBatch[%s, Qty: %d, Purchased: %s, Expires: %s]",
            itemCode, quantity, purchaseDate, expiryDate);
    }
}