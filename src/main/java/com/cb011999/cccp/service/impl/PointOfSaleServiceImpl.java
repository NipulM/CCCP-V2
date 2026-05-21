package com.cb011999.cccp.service.impl;

import com.cb011999.cccp.domain.enums.BillStatus;
import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.domain.model.Bill;
import com.cb011999.cccp.domain.model.Item;
import com.cb011999.cccp.domain.model.OnlineCustomer;
import com.cb011999.cccp.repository.BillRepository;
import com.cb011999.cccp.repository.ItemRepository;
import com.cb011999.cccp.service.InventoryService;
import com.cb011999.cccp.service.PointOfSaleService;
import com.cb011999.cccp.strategy.PaymentStrategy;

import java.util.Optional;

public class PointOfSaleServiceImpl implements PointOfSaleService {

    private final ItemRepository itemRepository;
    private final BillRepository billRepository;
    private final InventoryService inventoryService;

    public PointOfSaleServiceImpl(ItemRepository itemRepository,
                                  BillRepository billRepository,
                                  InventoryService inventoryService) {
        this.itemRepository = itemRepository;
        this.billRepository = billRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public Bill createBill(TransactionType transactionType, StoreType storeType) {
        return Bill.builder()
            .withTransactionType(transactionType)
            .withStoreType(storeType)
            .build();
    }

    @Override
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

    @Override
    public AddItemResult addItemToBill(Bill bill, String itemCode, int quantity) {
        Optional<Item> itemOpt = itemRepository.findByCode(itemCode);
        if (!itemOpt.isPresent()) {
            return new AddItemResult(false, "Item not found: " + itemCode);
        }

        Item item = itemOpt.get();

        int available = inventoryService.getTotalQuantity(itemCode, bill.getStoreType());
        if (quantity > available) {
            return new AddItemResult(false,
                String.format("Insufficient stock. Available: %d, Requested: %d",
                    available, quantity));
        }

        bill.addItem(item, quantity);
        return new AddItemResult(true, "Item added successfully");
    }

    @Override
    public CheckoutResult checkout(Bill bill, PaymentStrategy paymentStrategy,
                                   double cashTendered) {
        if (bill.getItems().isEmpty()) {
            return new CheckoutResult(false, "Cannot checkout empty bill", null);
        }

        bill.setPaymentMethod(paymentStrategy);

        if (cashTendered > 0) {
            bill.setCashPayment(cashTendered);
        }

        boolean paymentSuccess = bill.processPayment();

        if (!paymentSuccess) {
            return new CheckoutResult(false, "Payment failed", bill);
        }

        boolean stockReduced = reduceStockForBill(bill);

        if (!stockReduced) {
            bill.cancel();
            return new CheckoutResult(false, "Failed to reduce stock", bill);
        }

        billRepository.save(bill);
        return new CheckoutResult(true, "Checkout successful", bill);
    }

    @Override
    public CheckoutResult checkoutOnlineOrder(ShoppingCart cart, PaymentStrategy paymentStrategy) {
        Bill bill = cart.getBill();

        if (bill.getItems().isEmpty()) {
            return new CheckoutResult(false, "Shopping cart is empty", null);
        }

        bill.setPaymentMethod(paymentStrategy);

        boolean paymentSuccess = bill.processPayment();

        if (!paymentSuccess) {
            return new CheckoutResult(false, "Payment failed. Please check payment details.", bill);
        }

        boolean stockReduced = reduceStockForBill(bill);

        if (!stockReduced) {
            bill.cancel();
            return new CheckoutResult(false, "Some items are no longer available", bill);
        }

        billRepository.save(bill);
        return new CheckoutResult(true,
            "Order placed successfully! Delivery to: " + cart.getCustomer().getDeliveryAddress(),
            bill);
    }

    @Override
    public void applyDiscount(Bill bill, double discountAmount) {
        bill.applyDiscount(discountAmount);
    }

    @Override
    public boolean cancelBill(int serialNumber) {
        Optional<Bill> billOpt = billRepository.findBySerialNumber(serialNumber);
        if (!billOpt.isPresent()) {
            return false;
        }

        Bill bill = billOpt.get();
        if (bill.getStatus() == BillStatus.PAID) {
            return false;
        }

        bill.cancel();
        billRepository.save(bill);
        return true;
    }

    @Override
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
}