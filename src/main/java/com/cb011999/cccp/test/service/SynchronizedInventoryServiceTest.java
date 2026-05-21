package com.cb011999.cccp.test.service;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.model.StockBatch;
import com.cb011999.cccp.repository.impl.InMemoryInventoryRepository;
import com.cb011999.cccp.service.InventoryService;
import com.cb011999.cccp.service.concurrency.SynchronizedInventoryService;
import com.cb011999.cccp.service.impl.InventoryServiceImpl;

import java.time.LocalDate;

/**
 * Tests for SynchronizedInventoryService.
 * Verifies that the synchronized wrapper correctly delegates to the impl
 * and that concurrent access doesn't corrupt stock data.
 */
public class SynchronizedInventoryServiceTest {

    private InventoryService syncService;
    private InMemoryInventoryRepository inventoryRepo;

    @Before
    public void setUp() {
        inventoryRepo = InMemoryInventoryRepository.getInstance();
        inventoryRepo.clear();
        InventoryServiceImpl impl = new InventoryServiceImpl(inventoryRepo);
        syncService = new SynchronizedInventoryService(impl);
    }

    @Test
    public void testAddStockThroughQueue() {
        // Arrange
        String itemCode = "MILK001";
        LocalDate today = LocalDate.now();

        // Act — goes through the request queue
        syncService.addStockToWarehouse(itemCode, 100, today, today.plusDays(7));

        // Assert
        assertEquals(1, inventoryRepo.getStoreStock(itemCode).size());
        assertEquals(100, inventoryRepo.getStoreStock(itemCode).get(0).getQuantity());
    }

    @Test
    public void testReduceStockThroughQueue() {
        // Arrange
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 50, LocalDate.now(), LocalDate.now().plusDays(10)),
            StoreType.PHYSICAL_STORE
        );

        // Act — goes through the request queue
        boolean success = syncService.reduceStock("ITEM001", 20, StoreType.PHYSICAL_STORE);

        // Assert
        assertTrue(success);
        assertEquals(30, syncService.getTotalQuantity("ITEM001", StoreType.PHYSICAL_STORE));
    }

    @Test
    public void testReduceStockInsufficientThroughQueue() {
        // Arrange
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 10, LocalDate.now(), LocalDate.now().plusDays(10)),
            StoreType.PHYSICAL_STORE
        );

        // Act
        boolean success = syncService.reduceStock("ITEM001", 20, StoreType.PHYSICAL_STORE);

        // Assert
        assertFalse(success);
        assertEquals(10, syncService.getTotalQuantity("ITEM001", StoreType.PHYSICAL_STORE));
    }

    @Test
    public void testRestockShelvesThroughQueue() {
        // Arrange
        inventoryRepo.addToStore(
            new StockBatch("ITEM001", 100, LocalDate.now(), LocalDate.now().plusDays(30))
        );

        // Act
        int moved = syncService.restockShelves("ITEM001", 40, StoreType.PHYSICAL_STORE);

        // Assert
        assertEquals(40, moved);
        assertEquals(40, syncService.getTotalQuantity("ITEM001", StoreType.PHYSICAL_STORE));
    }

    @Test
    public void testConcurrentStockReductionMaintainsConsistency() throws InterruptedException {
        // Arrange — add 100 units to shelf
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 100, LocalDate.now(), LocalDate.now().plusDays(10)),
            StoreType.PHYSICAL_STORE
        );

        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        // Act — 10 threads each try to reduce stock by 5 simultaneously
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                syncService.reduceStock("ITEM001", 5, StoreType.PHYSICAL_STORE);
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // Assert — started with 100, reduced 5 x 10 = 50, should have exactly 50
        assertEquals(50, syncService.getTotalQuantity("ITEM001", StoreType.PHYSICAL_STORE));
    }

    @Test
    public void testConcurrentStockReductionPreventsNegativeStock() throws InterruptedException {
        // Arrange — add only 30 units
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 30, LocalDate.now(), LocalDate.now().plusDays(10)),
            StoreType.PHYSICAL_STORE
        );

        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        final int[] successCount = {0};
        final int[] failureCount = {0};
        final Object counterLock = new Object();

        // Act — 10 threads each try to reduce by 5 (total 50, but only 30 available)
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                boolean result = syncService.reduceStock("ITEM001", 5, StoreType.PHYSICAL_STORE);
                synchronized (counterLock) {
                    if (result) successCount[0]++;
                    else failureCount[0]++;
                }
            });
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // Assert — stock should never go negative
        int finalStock = syncService.getTotalQuantity("ITEM001", StoreType.PHYSICAL_STORE);
        assertTrue("Stock should not be negative", finalStock >= 0);

        // Exactly 6 should succeed (6 x 5 = 30) and 4 should fail
        assertEquals(6, successCount[0]);
        assertEquals(4, failureCount[0]);
        assertEquals(0, finalStock);
    }

    @Test
    public void testGetTotalQuantityReadOperation() {
        // Arrange
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 25, LocalDate.now(), LocalDate.now().plusDays(10)),
            StoreType.PHYSICAL_STORE
        );
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 35, LocalDate.now(), LocalDate.now().plusDays(15)),
            StoreType.PHYSICAL_STORE
        );

        // Act — read operation, synchronized but not queued
        int total = syncService.getTotalQuantity("ITEM001", StoreType.PHYSICAL_STORE);

        // Assert
        assertEquals(60, total);
    }
}