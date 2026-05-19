package com.cb011999.cccp.service;

import com.cb011999.cccp.domain.enums.BillStatus;
import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.domain.model.Bill;
import com.cb011999.cccp.domain.model.Item;
import com.cb011999.cccp.domain.model.OnlineCustomer;
import com.cb011999.cccp.repository.BillRepository;
import com.cb011999.cccp.repository.ItemRepository;
import com.cb011999.cccp.service.InventoryService;
import com.cb011999.cccp.strategy.PaymentStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PointOfSaleService {
    
    private final ItemRepository itemRepository;
    private final BillRepository billRepository;
    private final InventoryService inventoryService;
    
    public PointOfSaleService(ItemRepository itemRepository, 
                             BillRepository billRepository,
                             InventoryService inventoryService) {
        this.itemRepository = itemRepository;
        this.billRepository = billRepository;
        this.inventoryService = inventoryService;
    }
    
    public Bill createBill(TransactionType transactionType, StoreType storeType) {
        return Bill.builder()
            .withTransactionType(transactionType)
            .withStoreType(storeType)
            .build();
    }
    
    public ShoppingCart createOnlineShoppingCart(OnlineCustomer customer) {
        if (!customer.isRegistered()) {
            throw new IllegalStateException("Customer must be registered for online shopping");
        }
        
        Bill bill = Bill.builder()
            .withTransactionType(TransactionType.ONLINE)
            .withStoreType(StoreType.ONLINE_STORE)
            .build();
        
        return new ShoppingCart(bill, customer);
    }

    public AddItemResult addItemToBill(Bill bill, String itemCode, int quantity) {
        // Validate item exists
        Optional<Item> itemOpt = itemRepository.findByCode(itemCode);
        if (!itemOpt.isPresent()) {
            return new AddItemResult(false, "Item not found: " + itemCode);
        }
        
        Item item = itemOpt.get();
        
        // Check stock availability
        int available = inventoryService.getTotalQuantity(itemCode, bill.getStoreType());
        if (quantity > available) {
            return new AddItemResult(false, 
                String.format("Insufficient stock. Available: %d, Requested: %d", 
                    available, quantity));
        }
        
        // Add to bill
        bill.addItem(item, quantity);
        
        return new AddItemResult(true, "Item added successfully");
    }
    
    public CheckoutResult checkout(Bill bill, PaymentStrategy paymentStrategy, 
                                   double cashTendered) {
        if (bill.getItems().isEmpty()) {
            return new CheckoutResult(false, "Cannot checkout empty bill", null);
        }
        
        // Set payment method
        bill.setPaymentMethod(paymentStrategy);
        
        // For cash payments, set cash tendered
        if (cashTendered > 0) {
            bill.setCashPayment(cashTendered);
        }
        
        // Process payment
        boolean paymentSuccess = bill.processPayment();
        
        if (!paymentSuccess) {
            return new CheckoutResult(false, "Payment failed", bill);
        }
        
        // Reduce stock for all items in the bill
        boolean stockReduced = reduceStockForBill(bill);
        
        if (!stockReduced) {
            // Rollback - this shouldn't happen as we checked stock earlier
            bill.cancel();
            return new CheckoutResult(false, "Failed to reduce stock", bill);
        }
        
        // Save bill
        billRepository.save(bill);
        
        return new CheckoutResult(true, "Checkout successful", bill);
    }

    public CheckoutResult checkoutOnlineOrder(ShoppingCart cart, PaymentStrategy paymentStrategy) {
        Bill bill = cart.getBill();
        
        if (bill.getItems().isEmpty()) {
            return new CheckoutResult(false, "Shopping cart is empty", null);
        }
        
        // Set payment method
        bill.setPaymentMethod(paymentStrategy);
        
        // Process payment
        boolean paymentSuccess = bill.processPayment();
        
        if (!paymentSuccess) {
            return new CheckoutResult(false, "Payment failed. Please check payment details.", bill);
        }
        
        // Reduce stock from online inventory
        boolean stockReduced = reduceStockForBill(bill);
        
        if (!stockReduced) {
            bill.cancel();
            return new CheckoutResult(false, "Some items are no longer available", bill);
        }
        
        // Save bill
        billRepository.save(bill);
        
        return new CheckoutResult(true, 
            "Order placed successfully! Delivery to: " + cart.getCustomer().getDeliveryAddress(), 
            bill);
    }
    
    public void applyDiscount(Bill bill, double discountAmount) {
        bill.applyDiscount(discountAmount);
    }
    
    public boolean cancelBill(int serialNumber) {
        Optional<Bill> billOpt = billRepository.findBySerialNumber(serialNumber);
        if (!billOpt.isPresent()) {
            return false;
        }
        
        Bill bill = billOpt.get();
        if (bill.getStatus() == BillStatus.PAID) {
            // Cannot cancel paid bills (would need refund logic)
            return false;
        }
        
        bill.cancel();
        billRepository.save(bill);
        return true;
    }
    
    public Optional<Bill> getBill(int serialNumber) {
        return billRepository.findBySerialNumber(serialNumber);
    }

    private boolean reduceStockForBill(Bill bill) {
        for (var billItem : bill.getItems()) {
            boolean success = inventoryService.reduceStock(
                billItem.getItemCode(), 
                billItem.getQuantity(), 
                bill.getStoreType()
            );
            
            if (!success) {
                return false;
            }
        }
        return true;
    }
    
    public static class ShoppingCart {
        private final Bill bill;
        private final OnlineCustomer customer;
        private final List<String> notes;
        
        public ShoppingCart(Bill bill, OnlineCustomer customer) {
            this.bill = bill;
            this.customer = customer;
            this.notes = new ArrayList<>();
        }
        
        public Bill getBill() {
            return bill;
        }
        
        public OnlineCustomer getCustomer() {
            return customer;
        }
        
        public void addNote(String note) {
            notes.add(note);
        }
        
        public List<String> getNotes() {
            return notes;
        }
        
        public int getItemCount() {
            return bill.getItems().size();
        }
        
        public double getTotal() {
            return bill.getFinalAmount();
        }
    }

    public static class AddItemResult {
        private final boolean success;
        private final String message;
        
        public AddItemResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
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
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Bill getBill() {
            return bill;
        }
    }
}