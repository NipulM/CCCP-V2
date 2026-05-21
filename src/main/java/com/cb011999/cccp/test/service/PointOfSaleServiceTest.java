package com.cb011999.cccp.test.service;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.cb011999.cccp.domain.enums.BillStatus;
import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.domain.model.*;
import com.cb011999.cccp.repository.impl.*;
import com.cb011999.cccp.service.InventoryService;
import com.cb011999.cccp.service.PointOfSaleService;
import com.cb011999.cccp.service.impl.InventoryServiceImpl;
import com.cb011999.cccp.service.impl.PointOfSaleServiceImpl;
import com.cb011999.cccp.strategy.CashPayment;

import java.time.LocalDate;

/**
 * Clean tests for PointOfSaleService.
 * Tests the complete checkout flow and edge cases.
 */
public class PointOfSaleServiceTest {
    
    private PointOfSaleService posService;
    private InMemoryItemRepository itemRepo;
    private InMemoryBillRepository billRepo;
    private InMemoryInventoryRepository inventoryRepo;
    private InventoryService inventoryService;
    
    @Before
    public void setUp() {
        itemRepo = InMemoryItemRepository.getInstance();
        billRepo = InMemoryBillRepository.getInstance();
        inventoryRepo = InMemoryInventoryRepository.getInstance();
        
        itemRepo.clear();
        billRepo.clear();
        inventoryRepo.clear();
        Bill.resetRunningNumber();
        
        inventoryService = new InventoryServiceImpl(inventoryRepo); // ← only change needed
        posService = new PointOfSaleServiceImpl(itemRepo, billRepo, inventoryService);
        
        // Setup test data
        setupTestData();
    }
    
    private void setupTestData() {
        Category dairy = new Category(1, "Dairy");
        Item milk = new Item("MILK001", "Fresh Milk", 250.00, dairy);
        Item yogurt = new Item("YOGURT01", "Yogurt", 180.00, dairy);
        
        itemRepo.save(milk);
        itemRepo.save(yogurt);
        
        // Add stock to shelf
        inventoryRepo.addToInventory(
            new StockBatch("MILK001", 50, LocalDate.now(), LocalDate.now().plusDays(7)),
            StoreType.PHYSICAL_STORE
        );
        inventoryRepo.addToInventory(
            new StockBatch("YOGURT01", 40, LocalDate.now(), LocalDate.now().plusDays(14)),
            StoreType.PHYSICAL_STORE
        );
    }
    
    @Test
    public void testCreateBill() {
        // Act
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        
        // Assert
        assertNotNull(bill);
        assertEquals(TransactionType.OVER_THE_COUNTER, bill.getTransactionType());
        assertEquals(StoreType.PHYSICAL_STORE, bill.getStoreType());
        assertEquals(BillStatus.PENDING, bill.getStatus());
    }
    
    @Test
    public void testAddItemToBill_Success() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        
        // Act
        var result = posService.addItemToBill(bill, "MILK001", 2);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Item added successfully", result.getMessage());
        assertEquals(1, bill.getItems().size());
        assertEquals(500.00, bill.getTotalAmount(), 0.01);
    }
    
    @Test
    public void testAddItemToBill_ItemNotFound() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        
        // Act
        var result = posService.addItemToBill(bill, "INVALID", 2);
        
        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("not found"));
        assertEquals(0, bill.getItems().size());
    }
    
    @Test
    public void testAddItemToBill_InsufficientStock() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        
        // Act - Try to add more than available (50 in stock)
        var result = posService.addItemToBill(bill, "MILK001", 100);
        
        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Insufficient stock"));
        assertTrue(result.getMessage().contains("Available: 50"));
        assertEquals(0, bill.getItems().size());
    }
    
    @Test
    public void testCheckout_Success() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill, "MILK001", 2);
        
        CashPayment payment = new CashPayment(1000.00, bill.getFinalAmount());
        
        // Act
        var result = posService.checkout(bill, payment, 1000.00);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Checkout successful", result.getMessage());
        assertEquals(BillStatus.PAID, bill.getStatus());
        assertEquals(500.00, bill.getChange(), 0.01);
        
        // Stock should be reduced
        assertEquals(48, inventoryService.getTotalQuantity("MILK001", StoreType.PHYSICAL_STORE));
    }
    
    @Test
    public void testCheckout_EmptyBill() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        CashPayment payment = new CashPayment(1000.00, 0);
        
        // Act
        var result = posService.checkout(bill, payment, 1000.00);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Cannot checkout empty bill", result.getMessage());
    }
    
    @Test
    public void testCheckout_InsufficientPayment() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill, "MILK001", 2); // Total: 500
        
        CashPayment payment = new CashPayment(400.00, bill.getFinalAmount()); // Not enough
        
        // Act
        var result = posService.checkout(bill, payment, 400.00);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Payment failed", result.getMessage());
        assertEquals(BillStatus.PENDING, bill.getStatus());
        
        // Stock should NOT be reduced
        assertEquals(50, inventoryService.getTotalQuantity("MILK001", StoreType.PHYSICAL_STORE));
    }
    
    @Test
    public void testCheckout_WithDiscount() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill, "MILK001", 2); // Total: 500
        posService.applyDiscount(bill, 50.00); // Discount: 50
        
        CashPayment payment = new CashPayment(500.00, bill.getFinalAmount());
        
        // Act
        var result = posService.checkout(bill, payment, 500.00);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(450.00, bill.getFinalAmount(), 0.01);
        assertEquals(50.00, bill.getChange(), 0.01);
    }
    
    @Test
    public void testCheckout_MultipleItems() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill, "MILK001", 2);  // 500
        posService.addItemToBill(bill, "YOGURT01", 3); // 540
        
        CashPayment payment = new CashPayment(1500.00, bill.getFinalAmount());
        
        // Act
        var result = posService.checkout(bill, payment, 1500.00);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(1040.00, bill.getTotalAmount(), 0.01);
        assertEquals(460.00, bill.getChange(), 0.01);
        
        // Both stocks should be reduced
        assertEquals(48, inventoryService.getTotalQuantity("MILK001", StoreType.PHYSICAL_STORE));
        assertEquals(37, inventoryService.getTotalQuantity("YOGURT01", StoreType.PHYSICAL_STORE));
    }
    
    @Test
    public void testCheckout_BillIsSaved() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill, "MILK001", 2);
        
        CashPayment payment = new CashPayment(1000.00, bill.getFinalAmount());
        
        // Act
        posService.checkout(bill, payment, 1000.00);
        
        // Assert - Bill should be in repository
        assertTrue(billRepo.findBySerialNumber(bill.getSerialNumber()).isPresent());
    }
    
    @Test
    public void testApplyDiscount() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill, "MILK001", 2);
        
        // Act
        posService.applyDiscount(bill, 100.00);
        
        // Assert
        assertEquals(500.00, bill.getTotalAmount(), 0.01);
        assertEquals(100.00, bill.getDiscount(), 0.01);
        assertEquals(400.00, bill.getFinalAmount(), 0.01);
    }
    
    @Test
    public void testCancelBill_Pending() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill, "MILK001", 2);
        billRepo.save(bill);
        
        // Act
        boolean cancelled = posService.cancelBill(bill.getSerialNumber());
        
        // Assert
        assertTrue(cancelled);
        assertEquals(BillStatus.CANCELLED, bill.getStatus());
    }
    
    @Test
    public void testCancelBill_Paid() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill, "MILK001", 2);
        
        CashPayment payment = new CashPayment(1000.00, bill.getFinalAmount());
        posService.checkout(bill, payment, 1000.00);
        
        // Act
        boolean cancelled = posService.cancelBill(bill.getSerialNumber());
        
        // Assert - Cannot cancel paid bills
        assertFalse(cancelled);
        assertEquals(BillStatus.PAID, bill.getStatus());
    }
    
    @Test
    public void testGetBill() {
        // Arrange
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill, "MILK001", 2);
        billRepo.save(bill);
        
        // Act
        var retrieved = posService.getBill(bill.getSerialNumber());
        
        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals(bill.getSerialNumber(), retrieved.get().getSerialNumber());
    }
    
    @Test
    public void testOnlineTransaction() {
        // Arrange - Online store with separate inventory
        inventoryRepo.addToInventory(
            new StockBatch("MILK001", 30, LocalDate.now(), LocalDate.now().plusDays(7)),
            StoreType.ONLINE_STORE
        );
        
        Bill bill = posService.createBill(TransactionType.ONLINE, StoreType.ONLINE_STORE);
        posService.addItemToBill(bill, "MILK001", 2);
        
        CashPayment payment = new CashPayment(1000.00, bill.getFinalAmount());
        
        // Act
        var result = posService.checkout(bill, payment, 1000.00);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(TransactionType.ONLINE, bill.getTransactionType());
        
        // Online stock should be reduced, physical untouched
        assertEquals(28, inventoryService.getTotalQuantity("MILK001", StoreType.ONLINE_STORE));
        assertEquals(50, inventoryService.getTotalQuantity("MILK001", StoreType.PHYSICAL_STORE));
    }
}