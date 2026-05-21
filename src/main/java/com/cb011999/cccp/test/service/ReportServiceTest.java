package com.cb011999.cccp.test.service;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.domain.model.*;
import com.cb011999.cccp.repository.impl.*;
import com.cb011999.cccp.service.InventoryService;
import com.cb011999.cccp.service.PointOfSaleService;
import com.cb011999.cccp.service.ReportService;
import com.cb011999.cccp.service.impl.InventoryServiceImpl;
import com.cb011999.cccp.service.impl.PointOfSaleServiceImpl;
import com.cb011999.cccp.service.impl.ReportServiceImpl;
import com.cb011999.cccp.strategy.CashPayment;

import java.time.LocalDate;

public class ReportServiceTest {
    
    private ReportService reportService;
    private PointOfSaleService posService;
    private InventoryService inventoryService;
    
    private InMemoryItemRepository itemRepo;
    private InMemoryBillRepository billRepo;
    private InMemoryInventoryRepository inventoryRepo;
    
    @Before
    public void setUp() {
        // Create repositories
        itemRepo = InMemoryItemRepository.getInstance();
        billRepo = InMemoryBillRepository.getInstance();
        inventoryRepo = InMemoryInventoryRepository.getInstance();
        
        // Clear repositories
        itemRepo.clear();
        billRepo.clear();
        inventoryRepo.clear();
        Bill.resetRunningNumber();
        
        // Create services
        inventoryService = new InventoryServiceImpl(inventoryRepo);
        posService = new PointOfSaleServiceImpl(itemRepo, billRepo, inventoryService);
        reportService = new ReportServiceImpl(billRepo, itemRepo, inventoryService);
        
        // Setup test data
        setupTestData();
    }
    
    private void setupTestData() {
        // Create categories
        Category dairy = new Category(1, "Dairy");
        Category beverages = new Category(2, "Beverages");
        
        // Create items
        Item milk = new Item("MILK001", "Fresh Milk", 250.00, dairy);
        Item coke = new Item("COKE001", "Coca Cola", 350.00, beverages);
        Item bread = new Item("BREAD01", "White Bread", 120.00, dairy);
        
        itemRepo.save(milk);
        itemRepo.save(coke);
        itemRepo.save(bread);
        
        // Add stock to shelf
        inventoryRepo.addToInventory(
            new StockBatch("MILK001", 100, LocalDate.now(), LocalDate.now().plusDays(7)),
            StoreType.PHYSICAL_STORE
        );
        inventoryRepo.addToInventory(
            new StockBatch("COKE001", 80, LocalDate.now(), LocalDate.now().plusDays(180)),
            StoreType.PHYSICAL_STORE
        );
        inventoryRepo.addToInventory(
            new StockBatch("BREAD01", 30, LocalDate.now(), LocalDate.now().plusDays(3)),
            StoreType.PHYSICAL_STORE
        );
        
        // Add stock to warehouse
        inventoryRepo.addToStore(
            new StockBatch("MILK001", 200, LocalDate.now(), LocalDate.now().plusDays(7))
        );
        inventoryRepo.addToStore(
            new StockBatch("COKE001", 300, LocalDate.now(), LocalDate.now().plusDays(180))
        );
    }
    
    @Test
    public void testDailySalesReport_NoSales() {
        // Act
        var report = reportService.generateDailySalesReport(
            LocalDate.now(), null, null
        );
        
        // Assert
        assertNotNull(report);
        assertEquals(LocalDate.now(), report.getReportDate());
        assertEquals(0, report.getItems().size());
        assertEquals(0.0, report.getTotalRevenue(), 0.01);
    }
    
    @Test
    public void testDailySalesReport_WithSales() {
        // Arrange - Make 2 sales
        Bill bill1 = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill1, "MILK001", 2);
        posService.checkout(bill1, new CashPayment(600, 500), 600);
        
        Bill bill2 = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill2, "COKE001", 1);
        posService.addItemToBill(bill2, "MILK001", 1);
        posService.checkout(bill2, new CashPayment(700, 600), 700);
        
        // Act
        var report = reportService.generateDailySalesReport(
            LocalDate.now(), null, null
        );
        
        // Assert
        assertNotNull(report);
        assertEquals(2, report.getItems().size()); // 2 different items sold
        assertEquals(1100.0, report.getTotalRevenue(), 0.01); // 500 + 600
    }
    
    @Test
    public void testDailySalesReport_FilterByTransactionType() {
        // Arrange - Make OTC and Online sales
        Bill otcBill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(otcBill, "MILK001", 2);
        posService.checkout(otcBill, new CashPayment(600, 500), 600);
        
        // Add online inventory
        inventoryRepo.addToInventory(
            new StockBatch("MILK001", 50, LocalDate.now(), LocalDate.now().plusDays(7)),
            StoreType.ONLINE_STORE
        );
        
        Bill onlineBill = posService.createBill(TransactionType.ONLINE, StoreType.ONLINE_STORE);
        posService.addItemToBill(onlineBill, "MILK001", 1);
        posService.checkout(onlineBill, new CashPayment(300, 250), 300);
        
        // Act - Filter only OTC
        var report = reportService.generateDailySalesReport(
            LocalDate.now(), TransactionType.OVER_THE_COUNTER, null
        );
        
        // Assert - Should only show OTC sales
        assertEquals(500.0, report.getTotalRevenue(), 0.01);
    }
    
    @Test
    public void testReshelfReport_NoExpiringSoon() {
        // Act - Check items expiring within 1 day
        var report = reportService.generateReshelfReport(StoreType.PHYSICAL_STORE, 1);
        
        // Assert - Nothing expires in 1 day
        assertNotNull(report);
        assertEquals(0, report.getItems().size());
    }
    
    @Test
    public void testReshelfReport_WithExpiringSoon() {
        // Act - Check items expiring within 5 days
        var report = reportService.generateReshelfReport(StoreType.PHYSICAL_STORE, 5);
        
        // Assert - BREAD01 expires in 3 days
        assertNotNull(report);
        assertTrue("Should have items expiring soon", report.getItems().size() > 0);
        
        // Find bread in report
        boolean foundBread = report.getItems().stream()
            .anyMatch(item -> item.getItemCode().equals("BREAD01"));
        assertTrue("Should find bread in reshelf report", foundBread);
    }
    
    @Test
    public void testReorderReport_BelowThreshold() {
        // Arrange - Reduce BREAD01 stock to below 50
        inventoryService.reduceStock("BREAD01", 25, StoreType.PHYSICAL_STORE);
        
        // Act
        var report = reportService.generateReorderReport(StoreType.PHYSICAL_STORE);
        
        // Assert - BREAD01 should need reorder (now only 5 units)
        assertNotNull(report);
        assertTrue("Should have items needing reorder", report.getTotalItemsNeedingReorder() > 0);
        
        boolean foundBread = report.getItems().stream()
            .anyMatch(item -> item.getItemCode().equals("BREAD01"));
        assertTrue("BREAD01 should need reorder", foundBread);
    }
    
    @Test
    public void testReorderReport_AboveThreshold() {
        // Act - All items start with enough stock
        var report = reportService.generateReorderReport(StoreType.PHYSICAL_STORE);
        
        // Assert - Should be empty or minimal
        assertNotNull(report);
        // MILK001 has 100, COKE001 has 80, BREAD01 has 30
        // Only BREAD01 is below 50
        assertEquals(1, report.getTotalItemsNeedingReorder());
    }
    
    @Test
    public void testStockReport_NotEmpty() {
        // Act
        var report = reportService.generateStockReport();
        
        // Assert
        assertNotNull(report);
        assertTrue("Should have stock batches", report.getTotalBatches() > 0);
        assertTrue("Should have total quantity", report.getTotalQuantity() > 0);
    }
    
    @Test
    public void testStockReport_HasCorrectItems() {
        // Act
        var report = reportService.generateStockReport();
        
        // Assert
        boolean hasMilk = report.getBatches().stream()
            .anyMatch(batch -> batch.getItemCode().equals("MILK001"));
        boolean hasCoke = report.getBatches().stream()
            .anyMatch(batch -> batch.getItemCode().equals("COKE001"));
        
        assertTrue("Should have MILK001 in stock", hasMilk);
        assertTrue("Should have COKE001 in stock", hasCoke);
    }
    
    @Test
    public void testBillReport_Empty() {
        // Act
        var report = reportService.generateBillReport(null, null);
        
        // Assert
        assertNotNull(report);
        assertEquals(0, report.getTotalBills());
        assertEquals(0.0, report.getTotalRevenue(), 0.01);
    }
    
    @Test
    public void testBillReport_WithBills() {
        // Arrange - Create 2 bills
        Bill bill1 = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill1, "MILK001", 2);
        posService.checkout(bill1, new CashPayment(600, 500), 600);
        
        Bill bill2 = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(bill2, "COKE001", 1);
        posService.checkout(bill2, new CashPayment(400, 350), 400);
        
        // Act
        var report = reportService.generateBillReport(null, null);
        
        // Assert
        assertNotNull(report);
        assertEquals(2, report.getTotalBills());
        assertEquals(850.0, report.getTotalRevenue(), 0.01);
    }
    
    @Test
    public void testBillReport_FilterByTransactionType() {
        // Arrange - Create OTC and Online bills
        Bill otcBill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        posService.addItemToBill(otcBill, "MILK001", 2);
        posService.checkout(otcBill, new CashPayment(600, 500), 600);
        
        // Add online inventory
        inventoryRepo.addToInventory(
            new StockBatch("MILK001", 50, LocalDate.now(), LocalDate.now().plusDays(7)),
            StoreType.ONLINE_STORE
        );
        
        Bill onlineBill = posService.createBill(TransactionType.ONLINE, StoreType.ONLINE_STORE);
        posService.addItemToBill(onlineBill, "MILK001", 1);
        posService.checkout(onlineBill, new CashPayment(300, 250), 300);
        
        // Act - Filter only OTC
        var report = reportService.generateBillReport(TransactionType.OVER_THE_COUNTER, null);
        
        // Assert
        assertEquals(1, report.getTotalBills());
        assertEquals(500.0, report.getTotalRevenue(), 0.01);
    }
    
    @Test
    public void testAllReportsGenerate() {
        // Act - Generate all 5 reports
        var dailySales = reportService.generateDailySalesReport(LocalDate.now(), null, null);
        var reshelf = reportService.generateReshelfReport(StoreType.PHYSICAL_STORE, 5);
        var reorder = reportService.generateReorderReport(StoreType.PHYSICAL_STORE);
        var stock = reportService.generateStockReport();
        var bills = reportService.generateBillReport(null, null);
        
        // Assert - All should generate without errors
        assertNotNull("Daily sales report should generate", dailySales);
        assertNotNull("Reshelf report should generate", reshelf);
        assertNotNull("Reorder report should generate", reorder);
        assertNotNull("Stock report should generate", stock);
        assertNotNull("Bill report should generate", bills);
    }
}