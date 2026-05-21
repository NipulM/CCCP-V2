package com.cb011999.cccp.test.service;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.cb011999.cccp.domain.enums.BillStatus;
import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.domain.model.Bill;
import com.cb011999.cccp.domain.model.Category;
import com.cb011999.cccp.domain.model.Item;
import com.cb011999.cccp.domain.model.StockBatch;
import com.cb011999.cccp.repository.impl.InMemoryBillRepository;
import com.cb011999.cccp.repository.impl.InMemoryInventoryRepository;
import com.cb011999.cccp.repository.impl.InMemoryItemRepository;
import com.cb011999.cccp.service.PointOfSaleService;
import com.cb011999.cccp.service.concurrency.SynchronizedPointOfSaleService;
import com.cb011999.cccp.service.impl.InventoryServiceImpl;
import com.cb011999.cccp.service.impl.PointOfSaleServiceImpl;
import com.cb011999.cccp.strategy.CashPayment;

import java.time.LocalDate;

/**
 * Tests for SynchronizedPointOfSaleService.
 * Verifies that concurrent checkouts maintain stock consistency
 * and that the queue processes sales correctly.
 */
public class SynchronizedPointOfSaleServiceTest {

    private PointOfSaleService syncPosService;
    private InMemoryInventoryRepository inventoryRepo;
    private InMemoryItemRepository itemRepo;
    private InMemoryBillRepository billRepo;
    private InventoryServiceImpl inventoryImpl;

    @Before
    public void setUp() {
        inventoryRepo = InMemoryInventoryRepository.getInstance();
        inventoryRepo.clear();
        itemRepo = InMemoryItemRepository.getInstance();
        itemRepo.clear();
        billRepo = InMemoryBillRepository.getInstance();
        billRepo.clear();
        Bill.resetRunningNumber();

        inventoryImpl = new InventoryServiceImpl(inventoryRepo);
        PointOfSaleServiceImpl posImpl = new PointOfSaleServiceImpl(itemRepo, billRepo, inventoryImpl);
        syncPosService = new SynchronizedPointOfSaleService(posImpl);

        // Add test items
        itemRepo.save(new Item("MILK001", "Fresh Milk 1L", 250.00, new Category(1, "Dairy")));
        itemRepo.save(new Item("COKE001", "Coca Cola 1.5L", 350.00, new Category(2, "Beverages")));
    }

    @Test
    public void testCreateBillDoesNotGoThroughQueue() {
        // Act — bill creation is a simple object creation, no shared state
        Bill bill = syncPosService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);

        // Assert
        assertNotNull(bill);
        assertEquals(TransactionType.OVER_THE_COUNTER, bill.getTransactionType());
        assertEquals(StoreType.PHYSICAL_STORE, bill.getStoreType());
        assertEquals(BillStatus.PENDING, bill.getStatus());
    }

    @Test
    public void testAddItemThroughQueue() {
        // Arrange
        inventoryRepo.addToInventory(
            new StockBatch("MILK001", 50, LocalDate.now(), LocalDate.now().plusDays(7)),
            StoreType.PHYSICAL_STORE
        );
        Bill bill = syncPosService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);

        // Act — goes through the request queue
        PointOfSaleService.AddItemResult result = syncPosService.addItemToBill(bill, "MILK001", 3);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(1, bill.getItems().size());
        assertEquals(750.00, bill.getTotalAmount(), 0.01);
    }

    @Test
    public void testAddItemInsufficientStockThroughQueue() {
        // Arrange
        inventoryRepo.addToInventory(
            new StockBatch("MILK001", 5, LocalDate.now(), LocalDate.now().plusDays(7)),
            StoreType.PHYSICAL_STORE
        );
        Bill bill = syncPosService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);

        // Act
        PointOfSaleService.AddItemResult result = syncPosService.addItemToBill(bill, "MILK001", 10);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Insufficient stock"));
    }

    @Test
    public void testCheckoutThroughQueue() {
        // Arrange
        inventoryRepo.addToInventory(
            new StockBatch("MILK001", 50, LocalDate.now(), LocalDate.now().plusDays(7)),
            StoreType.PHYSICAL_STORE
        );
        Bill bill = syncPosService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        syncPosService.addItemToBill(bill, "MILK001", 2);

        // Act
        CashPayment payment = new CashPayment(1000.00, bill.getFinalAmount());
        PointOfSaleService.CheckoutResult result = syncPosService.checkout(bill, payment, 1000.00);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(BillStatus.PAID, result.getBill().getStatus());
        assertEquals(48, inventoryImpl.getTotalQuantity("MILK001", StoreType.PHYSICAL_STORE));
    }

    @Test
    public void testConcurrentCheckoutsMaintainStockConsistency() throws InterruptedException {
        // Arrange — 50 units of milk available
        inventoryRepo.addToInventory(
            new StockBatch("MILK001", 50, LocalDate.now(), LocalDate.now().plusDays(7)),
            StoreType.PHYSICAL_STORE
        );

        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        final int[] successCount = {0};
        final int[] failureCount = {0};
        final Object counterLock = new Object();

        // Act — 5 threads each try to buy 5 milk simultaneously
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                Bill bill = syncPosService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
                syncPosService.addItemToBill(bill, "MILK001", 5);

                CashPayment payment = new CashPayment(5000.00, bill.getFinalAmount());
                PointOfSaleService.CheckoutResult result = syncPosService.checkout(bill, payment, 5000.00);

                synchronized (counterLock) {
                    if (result.isSuccess()) successCount[0]++;
                    else failureCount[0]++;
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // Assert — all 5 should succeed (5 x 5 = 25, we have 50)
        assertEquals(5, successCount[0]);
        assertEquals(0, failureCount[0]);
        assertEquals(25, inventoryImpl.getTotalQuantity("MILK001", StoreType.PHYSICAL_STORE));
    }

    @Test
    public void testConcurrentCheckoutsPreventOverselling() throws InterruptedException {
        // Arrange — only 20 units available
        inventoryRepo.addToInventory(
            new StockBatch("MILK001", 20, LocalDate.now(), LocalDate.now().plusDays(7)),
            StoreType.PHYSICAL_STORE
        );

        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        final int[] successCount = {0};
        final int[] failureCount = {0};
        final Object counterLock = new Object();

        // Act — 10 threads each try to buy 5 (total 50, but only 20 available)
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                Bill bill = syncPosService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
                PointOfSaleService.AddItemResult addResult = syncPosService.addItemToBill(bill, "MILK001", 5);

                if (addResult.isSuccess()) {
                    CashPayment payment = new CashPayment(5000.00, bill.getFinalAmount());
                    PointOfSaleService.CheckoutResult result = syncPosService.checkout(bill, payment, 5000.00);

                    synchronized (counterLock) {
                        if (result.isSuccess()) successCount[0]++;
                        else failureCount[0]++;
                    }
                } else {
                    synchronized (counterLock) {
                        failureCount[0]++;
                    }
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // Assert — stock should never go negative
        int finalStock = inventoryImpl.getTotalQuantity("MILK001", StoreType.PHYSICAL_STORE);
        assertTrue("Stock should not be negative", finalStock >= 0);

        // Only 4 should succeed (4 x 5 = 20)
        assertEquals(4, successCount[0]);
        assertEquals(6, failureCount[0]);
        assertEquals(0, finalStock);
    }

    @Test
    public void testApplyDiscountDoesNotGoThroughQueue() {
        // Arrange
        inventoryRepo.addToInventory(
            new StockBatch("MILK001", 50, LocalDate.now(), LocalDate.now().plusDays(7)),
            StoreType.PHYSICAL_STORE
        );
        Bill bill = syncPosService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        syncPosService.addItemToBill(bill, "MILK001", 2);

        // Act
        syncPosService.applyDiscount(bill, 100.00);

        // Assert — 2 x 250 = 500, minus 100 discount = 400
        assertEquals(400.00, bill.getFinalAmount(), 0.01);
    }
}