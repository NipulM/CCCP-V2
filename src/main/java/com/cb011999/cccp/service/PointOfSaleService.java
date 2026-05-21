package com.cb011999.cccp.service;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.domain.model.Bill;
import com.cb011999.cccp.domain.model.OnlineCustomer;
import com.cb011999.cccp.strategy.PaymentStrategy;

import java.util.Optional;

public interface PointOfSaleService {

    Bill createBill(TransactionType transactionType, StoreType storeType);

    ShoppingCart createOnlineShoppingCart(OnlineCustomer customer);

    AddItemResult addItemToBill(Bill bill, String itemCode, int quantity);

    CheckoutResult checkout(Bill bill, PaymentStrategy paymentStrategy, double cashTendered);

    CheckoutResult checkoutOnlineOrder(ShoppingCart cart, PaymentStrategy paymentStrategy);

    void applyDiscount(Bill bill, double discountAmount);

    boolean cancelBill(int serialNumber);

    Optional<Bill> getBill(int serialNumber);

    // Inner classes stay here on the interface so everyone can reference them

    public static class ShoppingCart {
        private final Bill bill;
        private final OnlineCustomer customer;
        private final java.util.List<String> notes;

        public ShoppingCart(Bill bill, OnlineCustomer customer) {
            this.bill = bill;
            this.customer = customer;
            this.notes = new java.util.ArrayList<>();
        }

        public Bill getBill() { return bill; }
        public OnlineCustomer getCustomer() { return customer; }
        public void addNote(String note) { notes.add(note); }
        public java.util.List<String> getNotes() { return notes; }
        public int getItemCount() { return bill.getItems().size(); }
        public double getTotal() { return bill.getFinalAmount(); }
    }

    public static class AddItemResult {
        private final boolean success;
        private final String message;

        public AddItemResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    public static class CheckoutResult {
        private final boolean success;
        private final String message;
        private final Bill bill;

        public CheckoutResult(boolean success, String message, Bill bill) {
            this.success = success;
            this.message = message;
            this.bill = bill;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Bill getBill() { return bill; }
    }
}