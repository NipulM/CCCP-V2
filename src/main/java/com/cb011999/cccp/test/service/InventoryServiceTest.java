package com.cb011999.cccp.test.service;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.model.StockBatch;
import com.cb011999.cccp.repository.impl.InMemoryInventoryRepository;
import com.cb011999.cccp.service.InventoryService;
import com.cb011999.cccp.service.impl.InventoryServiceImpl;

import java.time.LocalDate;
import java.util.List;

/**
 * Clean tests for InventoryService.
 * Tests follow the AAA pattern: Arrange, Act, Assert.
 * Each test focuses on a single behavior.
 */
public class InventoryServiceTest {
    
    private InventoryService inventoryService;
    private InMemoryInventoryRepository inventoryRepo;
    
    @Before
    public void setUp() {
        inventoryRepo = InMemoryInventoryRepository.getInstance();
        inventoryRepo.clear();
        inventoryService = new InventoryServiceImpl(inventoryRepo);
    }
    
    @Test
    public void testAddStockToWarehouse() {
        // Arrange
        String itemCode = "MILK001";
        int quantity = 100;
        LocalDate purchaseDate = LocalDate.now();
        LocalDate expiryDate = LocalDate.now().plusDays(7);
        
        // Act
        inventoryService.addStockToWarehouse(itemCode, quantity, purchaseDate, expiryDate);
        
        // Assert
        List<StockBatch> stock = inventoryRepo.getStoreStock(itemCode);
        assertEquals(1, stock.size());
        assertEquals(100, stock.get(0).getQuantity());
    }
    
    @Test
    public void testRestockShelves_UsesOldestBatch() {
        // Arrange - Add two batches with different purchase dates
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today.plusDays(30);
        
        inventoryRepo.addToStore(new StockBatch("ITEM001", 50, today.minusDays(5), expiryDate));
        inventoryRepo.addToStore(new StockBatch("ITEM001", 50, today, expiryDate));
        
        // Act - Restock 30 units to shelf
        int moved = inventoryService.restockShelves("ITEM001", 30, StoreType.PHYSICAL_STORE);
        
        // Assert - Should use oldest batch first
        assertEquals(30, moved);
        List<StockBatch> shelfStock = inventoryRepo.getShelfStock("ITEM001");
        assertEquals(1, shelfStock.size());
        assertEquals(30, shelfStock.get(0).getQuantity());
        
        // Warehouse should have 70 remaining
        List<StockBatch> storeStock = inventoryRepo.getStoreStock("ITEM001");
        assertEquals(20, storeStock.get(0).getQuantity()); // Oldest reduced to 20
        assertEquals(50, storeStock.get(1).getQuantity()); // Newer untouched
    }
    
    @Test
    public void testRestockShelves_UsesCloserExpiryFirst() {
        // Arrange - Add two batches: older with far expiry, newer with close expiry
        LocalDate today = LocalDate.now();
        
        inventoryRepo.addToStore(new StockBatch("ITEM001", 50, today.minusDays(10), today.plusDays(60))); // Old, far expiry
        inventoryRepo.addToStore(new StockBatch("ITEM001", 50, today.minusDays(2), today.plusDays(5)));   // New, close expiry
        
        // Act
        int moved = inventoryService.restockShelves("ITEM001", 30, StoreType.PHYSICAL_STORE);
        
        // Assert - Should prioritize closer expiry date
        assertEquals(30, moved);
        List<StockBatch> shelfStock = inventoryRepo.getShelfStock("ITEM001");
        
        // Should have taken from batch with closer expiry (5 days)
        assertEquals(today.plusDays(5), shelfStock.get(0).getExpiryDate());
    }
    
    @Test
    public void testReduceStock_Success() {
        // Arrange
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 50, LocalDate.now(), LocalDate.now().plusDays(10)),
            StoreType.PHYSICAL_STORE
        );
        
        // Act
        boolean success = inventoryService.reduceStock("ITEM001", 30, StoreType.PHYSICAL_STORE);
        
        // Assert
        assertTrue(success);
        assertEquals(20, inventoryService.getTotalQuantity("ITEM001", StoreType.PHYSICAL_STORE));
    }
    
    @Test
    public void testReduceStock_InsufficientStock() {
        // Arrange
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 20, LocalDate.now(), LocalDate.now().plusDays(10)),
            StoreType.PHYSICAL_STORE
        );
        
        // Act
        boolean success = inventoryService.reduceStock("ITEM001", 30, StoreType.PHYSICAL_STORE);
        
        // Assert
        assertFalse(success);
        assertEquals(20, inventoryService.getTotalQuantity("ITEM001", StoreType.PHYSICAL_STORE));
    }
    
    @Test
    public void testReduceStock_UsesFEFO() {
        // Arrange - Add batches with different expiry dates
        LocalDate today = LocalDate.now();
        
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 30, today, today.plusDays(15)), // Later expiry
            StoreType.PHYSICAL_STORE
        );
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 30, today, today.plusDays(5)),  // Earlier expiry
            StoreType.PHYSICAL_STORE
        );
        
        // Act - Reduce 25 units
        inventoryService.reduceStock("ITEM001", 25, StoreType.PHYSICAL_STORE);
        
        // Assert - Should take from earlier expiry first
        List<StockBatch> stock = inventoryRepo.getShelfStock("ITEM001");
        
        // First batch (earlier expiry) should have 5 left
        StockBatch earlierExpiryBatch = stock.stream()
            .filter(b -> b.getExpiryDate().equals(today.plusDays(5)))
            .findFirst().get();
        assertEquals(5, earlierExpiryBatch.getQuantity());
        
        // Second batch should be untouched
        StockBatch laterExpiryBatch = stock.stream()
            .filter(b -> b.getExpiryDate().equals(today.plusDays(15)))
            .findFirst().get();
        assertEquals(30, laterExpiryBatch.getQuantity());
    }
    
    @Test
    public void testGetTotalQuantity() {
        // Arrange
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 30, LocalDate.now(), LocalDate.now().plusDays(10)),
            StoreType.PHYSICAL_STORE
        );
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 20, LocalDate.now(), LocalDate.now().plusDays(15)),
            StoreType.PHYSICAL_STORE
        );
        
        // Act
        int total = inventoryService.getTotalQuantity("ITEM001", StoreType.PHYSICAL_STORE);
        
        // Assert
        assertEquals(50, total);
    }
    
    @Test
    public void testNeedsReorder_BelowThreshold() {
        // Arrange - Add stock below threshold (50)
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 30, LocalDate.now(), LocalDate.now().plusDays(10)),
            StoreType.PHYSICAL_STORE
        );
        
        // Act & Assert
        assertTrue(inventoryService.needsReorder("ITEM001", StoreType.PHYSICAL_STORE));
    }
    
    @Test
    public void testNeedsReorder_AboveThreshold() {
        // Arrange - Add stock above threshold (50)
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 60, LocalDate.now(), LocalDate.now().plusDays(10)),
            StoreType.PHYSICAL_STORE
        );
        
        // Act & Assert
        assertFalse(inventoryService.needsReorder("ITEM001", StoreType.PHYSICAL_STORE));
    }
    
    @Test
    public void testGetItemsNeedingReorder() {
        // Arrange
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 30, LocalDate.now(), LocalDate.now().plusDays(10)), // Below threshold
            StoreType.PHYSICAL_STORE
        );
        inventoryRepo.addToInventory(
            new StockBatch("ITEM002", 60, LocalDate.now(), LocalDate.now().plusDays(10)), // Above threshold
            StoreType.PHYSICAL_STORE
        );
        inventoryRepo.addToInventory(
            new StockBatch("ITEM003", 40, LocalDate.now(), LocalDate.now().plusDays(10)), // Below threshold
            StoreType.PHYSICAL_STORE
        );
        
        // Act
        List<String> needsReorder = inventoryService.getItemsNeedingReorder(StoreType.PHYSICAL_STORE);
        
        // Assert
        assertEquals(2, needsReorder.size());
        assertTrue(needsReorder.contains("ITEM001"));
        assertTrue(needsReorder.contains("ITEM003"));
        assertFalse(needsReorder.contains("ITEM002"));
    }
    
    @Test
    public void testGetItemsToReshelf() {
        // Arrange
        LocalDate today = LocalDate.now();
        
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 30, today, today.plusDays(2)), // Expires soon
            StoreType.PHYSICAL_STORE
        );
        inventoryRepo.addToInventory(
            new StockBatch("ITEM002", 40, today, today.plusDays(10)), // Expires later
            StoreType.PHYSICAL_STORE
        );
        
        // Act - Get items expiring within 3 days
        List<StockBatch> toReshelf = inventoryService.getItemsToReshelf(StoreType.PHYSICAL_STORE, 3);
        
        // Assert
        assertEquals(1, toReshelf.size());
        assertEquals("ITEM001", toReshelf.get(0).getItemCode());
    }
    
    @Test
    public void testSeparateInventoriesForStoreTypes() {
        // Arrange
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 30, LocalDate.now(), LocalDate.now().plusDays(10)),
            StoreType.PHYSICAL_STORE
        );
        inventoryRepo.addToInventory(
            new StockBatch("ITEM001", 20, LocalDate.now(), LocalDate.now().plusDays(10)),
            StoreType.ONLINE_STORE
        );
        
        // Act & Assert
        assertEquals(30, inventoryService.getTotalQuantity("ITEM001", StoreType.PHYSICAL_STORE));
        assertEquals(20, inventoryService.getTotalQuantity("ITEM001", StoreType.ONLINE_STORE));
    }
}