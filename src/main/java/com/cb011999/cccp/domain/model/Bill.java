package com.cb011999.cccp.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cb011999.cccp.domain.enums.BillStatus;
import com.cb011999.cccp.domain.enums.PaymentMethod;
import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.strategy.PaymentStrategy;

public class Bill {
    private static int runningNumber = 1;
    
    private final int serialNumber;
    private final LocalDateTime billDate;
    private final List<BillItem> items;
    private final TransactionType transactionType;
    private final StoreType storeType;
    private double totalAmount;
    private double discount;
    private PaymentStrategy paymentMethod;
    private PaymentMethod paymentMethodType;
    private BillStatus status;
    private double cashTendered;
    private double change;
    
    private Bill(TransactionType transactionType, StoreType storeType) {
        this.serialNumber = runningNumber++;
        this.billDate = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.transactionType = transactionType;
        this.storeType = storeType;
        this.status = BillStatus.PENDING;
        this.totalAmount = 0.0;
        this.discount = 0.0;
    }

    /**
     * Constructor for loading from persistence (e.g. database).
     * Keeps runningNumber in sync so new bills get serial numbers above loaded ones.
     */
    private Bill(int serialNumber, LocalDateTime billDate, TransactionType transactionType, StoreType storeType) {
        this.serialNumber = serialNumber;
        this.billDate = billDate;
        this.items = new ArrayList<>();
        this.transactionType = transactionType;
        this.storeType = storeType;
        this.status = BillStatus.PENDING;
        this.totalAmount = 0.0;
        this.discount = 0.0;
        runningNumber = Math.max(runningNumber, serialNumber + 1);
    }

    /**
     * Creates a Bill from persisted data (e.g. database). Used by BillRepository implementations.
     */
    public static Bill fromPersisted(int serialNumber, LocalDateTime billDate,
                                    TransactionType transactionType, StoreType storeType,
                                    double totalAmount, double discount, BillStatus status,
                                    double cashTendered, double change) {
        Bill bill = new Bill(serialNumber, billDate, transactionType, storeType);
        bill.setTotalAmount(totalAmount);
        bill.setDiscount(discount);
        bill.setStatus(status);
        bill.setCashTendered(cashTendered);
        bill.setChange(change);
        return bill;
    }

    public void addItem(Item item, int quantity) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        double itemTotal = item.getUnitPrice() * quantity;
        items.add(new BillItem(item.getName(), item.getItemCode(), quantity, item.getUnitPrice()));
        this.totalAmount += itemTotal;
    }

    /**
     * Adds a bill item when loading from persistence (e.g. database).
     * Used by BillRepository implementations; do not use for normal POS flow.
     */
    public void addLoadedItem(BillItem billItem) {
        if (billItem == null) {
            throw new IllegalArgumentException("Bill item cannot be null");
        }
        items.add(billItem);
        this.totalAmount += billItem.getTotalPrice();
    }

    public void applyDiscount(double discount) {
        if (discount < 0) {
            throw new IllegalArgumentException("Discount cannot be negative");
        }
        this.discount = discount;
    }
    
    public void setPaymentMethod(PaymentStrategy paymentMethod) {
        this.paymentMethod = paymentMethod;
        this.paymentMethodType = paymentMethod != null ? paymentMethod.getPaymentMethodType() : null;
    }

    public boolean processPayment() {
        if (paymentMethod == null) {
            throw new IllegalStateException("Payment method not set");
        }
        
        double finalAmount = getFinalAmount();
        boolean success = paymentMethod.processPayment(finalAmount);
        
        if (success) {
            this.status = BillStatus.PAID;
        }
        
        return success;
    }
    
    public void setCashPayment(double cashTendered) {
        this.cashTendered = cashTendered;
        this.change = cashTendered - getFinalAmount();
    }
    
    public double getFinalAmount() {
        return totalAmount - discount;
    }
    

    public void cancel() {
        this.status = BillStatus.CANCELLED;
    }
    
    // Getters
    public int getSerialNumber() {
        return serialNumber;
    }
    
    public LocalDateTime getBillDate() {
        return billDate;
    }
    
    public List<BillItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public double getDiscount() {
        return discount;
    }

    /** For repository loading only. */
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    /** For repository loading only. */
    public void setDiscount(double discount) {
        this.discount = discount;
    }

    /** For repository loading only. */
    public void setStatus(BillStatus status) {
        this.status = status;
    }

    /** For repository loading only. */
    public void setCashTendered(double cashTendered) {
        this.cashTendered = cashTendered;
    }

    /** For repository loading only. */
    public void setChange(double change) {
        this.change = change;
    }
    
    public BillStatus getStatus() {
        return status;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public StoreType getStoreType() {
        return storeType;
    }
    
    public double getCashTendered() {
        return cashTendered;
    }
    
    public double getChange() {
        return change;
    }
    
    public PaymentStrategy getPaymentMethod() {
        return paymentMethod;
    }

    /** How the bill was paid (Cash, Card, Online). For display and reporting. */
    public PaymentMethod getPaymentMethodType() {
        return paymentMethodType;
    }

    /** For repository loading only. */
    public void setPaymentMethodType(PaymentMethod paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }
    
    public static class BillBuilder {
        private TransactionType transactionType;
        private StoreType storeType;
        
        public BillBuilder withTransactionType(TransactionType type) {
            this.transactionType = type;
            return this;
        }
        
        public BillBuilder withStoreType(StoreType type) {
            this.storeType = type;
            return this;
        }
        
        public Bill build() {
            if (transactionType == null) {
                throw new IllegalStateException("Transaction type must be set");
            }
            if (storeType == null) {
                throw new IllegalStateException("Store type must be set");
            }
            return new Bill(transactionType, storeType);
        }
    }
    
    public static BillBuilder builder() {
        return new BillBuilder();
    }
    
    public static void resetRunningNumber() {
        runningNumber = 1;
    }
}