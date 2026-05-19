package com.cb011999.cccp.observer;

import com.cb011999.cccp.domain.enums.StoreType;

public class EmailStockObserver implements StockObserver {
    private final String recipientEmail;
    
    public EmailStockObserver(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
    
    @Override
    public void onStockChanged(String itemCode, int currentQuantity, StoreType storeType) {
        String subject = "Stock Update Notification";
        String body = String.format(
            "Stock level changed for %s in %s. Current quantity: %d units.",
            itemCode, storeType.getDisplayName(), currentQuantity
        );
        sendEmail(subject, body);
    }
    
    @Override
    public void onLowStock(String itemCode, int currentQuantity, StoreType storeType) {
        String subject = "⚠️ LOW STOCK ALERT";
        String body = String.format(
            "URGENT: Item %s in %s has fallen below reorder threshold!\n" +
            "Current quantity: %d units\n" +
            "Recommended action: Place reorder immediately.",
            itemCode, storeType.getDisplayName(), currentQuantity
        );
        sendEmail(subject, body);
    }
    
    @Override
    public void onStockDepleted(String itemCode, StoreType storeType) {
        String subject = "🚨 CRITICAL: Out of Stock";
        String body = String.format(
            "CRITICAL ALERT: Item %s in %s is completely out of stock!\n" +
            "Immediate action required.",
            itemCode, storeType.getDisplayName()
        );
        sendEmail(subject, body);
    }
    
    private void sendEmail(String subject, String body) {
        System.out.println("📧 [EMAIL to " + recipientEmail + "]");
        System.out.println("   Subject: " + subject);
        System.out.println("   Body: " + body);
        System.out.println();
    }
}