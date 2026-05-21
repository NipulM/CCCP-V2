package com.cb011999.cccp.service.concurrency;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.domain.model.Bill;
import com.cb011999.cccp.domain.model.OnlineCustomer;
import com.cb011999.cccp.service.PointOfSaleService;
import com.cb011999.cccp.service.impl.PointOfSaleServiceImpl;
import com.cb011999.cccp.strategy.PaymentStrategy;
import com.cb011999.cccp.web.concurrency.RequestQueue;
import com.cb011999.cccp.web.concurrency.Task;

import java.util.Optional;

public class SynchronizedPointOfSaleService implements PointOfSaleService {

    private final PointOfSaleServiceImpl delegate;
    private final RequestQueue requestQueue;
    private final Object checkoutLock = new Object();

    public SynchronizedPointOfSaleService(PointOfSaleServiceImpl delegate) {
        this.delegate = delegate;
        this.requestQueue = RequestQueue.getInstance();
    }

    @Override
    public Bill createBill(TransactionType transactionType, StoreType storeType) {
        // No concurrency needed — just creates an object, no shared state
        return delegate.createBill(transactionType, storeType);
    }

    @Override
    public ShoppingCart createOnlineShoppingCart(OnlineCustomer customer) {
        return delegate.createOnlineShoppingCart(customer);
    }

    @Override
    public AddItemResult addItemToBill(Bill bill, String itemCode, int quantity) {
        final Task[] holder = new Task[1];
        holder[0] = new Task(() -> {
            synchronized (checkoutLock) {
                AddItemResult result = delegate.addItemToBill(bill, itemCode, quantity);
                holder[0].setResult(result);
            }
        });
        requestQueue.submitTask(holder[0]);

        if (holder[0].hasError()) {
            return new AddItemResult(false, "Server error: " + holder[0].getError());
        }
        return (AddItemResult) holder[0].getResult();
    }

    @Override
    public CheckoutResult checkout(Bill bill, PaymentStrategy paymentStrategy,
                                   double cashTendered) {
        final Task[] holder = new Task[1];
        holder[0] = new Task(() -> {
            synchronized (checkoutLock) {
                CheckoutResult result = delegate.checkout(bill, paymentStrategy, cashTendered);
                holder[0].setResult(result);
            }
        });
        requestQueue.submitTask(holder[0]);

        if (holder[0].hasError()) {
            return new CheckoutResult(false, "Server error: " + holder[0].getError(), null);
        }
        return (CheckoutResult) holder[0].getResult();
    }

    @Override
    public CheckoutResult checkoutOnlineOrder(ShoppingCart cart, PaymentStrategy paymentStrategy) {
        final Task[] holder = new Task[1];
        holder[0] = new Task(() -> {
            synchronized (checkoutLock) {
                CheckoutResult result = delegate.checkoutOnlineOrder(cart, paymentStrategy);
                holder[0].setResult(result);
            }
        });
        requestQueue.submitTask(holder[0]);

        if (holder[0].hasError()) {
            return new CheckoutResult(false, "Server error: " + holder[0].getError(), null);
        }
        return (CheckoutResult) holder[0].getResult();
    }

    @Override
    public void applyDiscount(Bill bill, double discountAmount) {
        // Simple setter — no shared state risk
        delegate.applyDiscount(bill, discountAmount);
    }

    @Override
    public boolean cancelBill(int serialNumber) {
        final Task[] holder = new Task[1];
        holder[0] = new Task(() -> {
            synchronized (checkoutLock) {
                boolean result = delegate.cancelBill(serialNumber);
                holder[0].setResult(result);
            }
        });
        requestQueue.submitTask(holder[0]);

        if (holder[0].hasError()) return false;
        Object result = holder[0].getResult();
        return result != null ? (boolean) result : false;
    }

    @Override
    public Optional<Bill> getBill(int serialNumber) {
        synchronized (checkoutLock) {
            return delegate.getBill(serialNumber);
        }
    }
}