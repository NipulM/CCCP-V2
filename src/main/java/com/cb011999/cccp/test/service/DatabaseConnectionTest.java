package com.cb011999.cccp.test.service;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import com.cb011999.cccp.database.DatabaseConnection;
import com.cb011999.cccp.domain.model.Category;
import com.cb011999.cccp.domain.model.Item;
import com.cb011999.cccp.repository.ItemRepository;
import com.cb011999.cccp.repository.impl.DatabaseItemRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Tests for database connectivity and operations.
 * These tests require a database to be set up and running.
 */
public class DatabaseConnectionTest {
    
    private DatabaseConnection dbConnection;
    private static final int TEST_CATEGORY_ID = 99;
    private static final String TEST_CATEGORY_NAME = "Test Category";
    
    @Before
    public void setUp() {
        dbConnection = DatabaseConnection.getInstance();
        // Ensure test category exists for tests that need it
        createTestCategoryIfNotExists();
    }
    
    @After
    public void tearDown() {
        // Clean up any test items that might remain
        cleanupTestItems();
    }
    
    /**
     * Helper method to create test category in database if it doesn't exist
     */
    private void createTestCategoryIfNotExists() {
        String sql = "INSERT IGNORE INTO categories (id, name, parent_category_id) VALUES (?, ?, NULL)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, TEST_CATEGORY_ID);
            pstmt.setString(2, TEST_CATEGORY_NAME);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Warning: Could not create test category: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to clean up test items
     */
    private void cleanupTestItems() {
        ItemRepository itemRepo = DatabaseItemRepository.getInstance();
        String[] testCodes = {"TEST001", "TEST002", "TEST003", "TEST004"};
        for (String code : testCodes) {
            try {
                itemRepo.delete(code);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }
    
    @Test
    public void testDatabaseConnectionExists() {
        // Assert
        assertNotNull("Database connection should not be null", dbConnection);
    }
    
    @Test
    public void testGetConnection() {
        // Act
        Connection conn = dbConnection.getConnection();
        
        // Assert
        assertNotNull("Connection should not be null", conn);
    }
    
    @Test
    public void testSingletonPattern() {
        // Act
        DatabaseConnection instance1 = DatabaseConnection.getInstance();
        DatabaseConnection instance2 = DatabaseConnection.getInstance();
        
        // Assert - Both should be the same instance
        assertSame("Should return same instance (Singleton)", instance1, instance2);
    }
    
    @Test
    public void testDatabaseInitialization() {
        // Act - Initialize database schema
        dbConnection.initializeDatabase();
        
        // Assert - If no exception thrown, initialization succeeded
        assertTrue("Database initialization should succeed", true);
    }
    
    @Test
    public void testDatabaseItemRepositorySingleton() {
        // Act
        ItemRepository repo1 = DatabaseItemRepository.getInstance();
        ItemRepository repo2 = DatabaseItemRepository.getInstance();
        
        // Assert
        assertSame("Should return same instance", repo1, repo2);
    }
    
    @Test
    public void testSaveAndFindItem() {
        // Arrange
        ItemRepository itemRepo = DatabaseItemRepository.getInstance();
        Category testCategory = new Category(TEST_CATEGORY_ID, TEST_CATEGORY_NAME);
        Item testItem = new Item("TEST001", "Test Item", 100.00, testCategory);
        
        try {
            // Act
            itemRepo.save(testItem);
            Optional<Item> found = itemRepo.findByCode("TEST001");
            
            // Assert
            assertTrue("Item should be found", found.isPresent());
            assertEquals("TEST001", found.get().getItemCode());
            assertEquals("Test Item", found.get().getName());
            assertEquals(100.00, found.get().getUnitPrice(), 0.01);
        } finally {
            // Cleanup
            itemRepo.delete("TEST001");
        }
    }
    
    @Test
    public void testUpdateItem() {
        // Arrange
        ItemRepository itemRepo = DatabaseItemRepository.getInstance();
        Category testCategory = new Category(TEST_CATEGORY_ID, TEST_CATEGORY_NAME);
        Item testItem = new Item("TEST002", "Original Name", 100.00, testCategory);
        
        try {
            // Act - Save original
            itemRepo.save(testItem);
            
            // Update
            testItem.setName("Updated Name");
            testItem.setUnitPrice(150.00);
            itemRepo.save(testItem);
            
            // Retrieve
            Optional<Item> found = itemRepo.findByCode("TEST002");
            
            // Assert
            assertTrue("Item should be found", found.isPresent());
            assertEquals("Updated Name", found.get().getName());
            assertEquals(150.00, found.get().getUnitPrice(), 0.01);
        } finally {
            // Cleanup
            itemRepo.delete("TEST002");
        }
    }
    
    @Test
    public void testDeleteItem() {
        // Arrange
        ItemRepository itemRepo = DatabaseItemRepository.getInstance();
        Category testCategory = new Category(TEST_CATEGORY_ID, TEST_CATEGORY_NAME);
        Item testItem = new Item("TEST003", "To Delete", 100.00, testCategory);
        
        // Act
        itemRepo.save(testItem);
        boolean deleted = itemRepo.delete("TEST003");
        Optional<Item> found = itemRepo.findByCode("TEST003");
        
        // Assert
        assertTrue("Delete should succeed", deleted);
        assertFalse("Item should not be found after deletion", found.isPresent());
    }
    
    @Test
    public void testFindAllItems() {
        // Arrange
        ItemRepository itemRepo = DatabaseItemRepository.getInstance();
        
        // Act
        List<Item> allItems = itemRepo.findAll();
        
        // Assert
        assertNotNull("Items list should not be null", allItems);
        // If sample data loaded, should have items
        System.out.println("Total items in database: " + allItems.size());
    }
    
    @Test
    public void testItemExists() {
        // Arrange
        ItemRepository itemRepo = DatabaseItemRepository.getInstance();
        Category testCategory = new Category(TEST_CATEGORY_ID, TEST_CATEGORY_NAME);
        Item testItem = new Item("TEST004", "Exists Test", 100.00, testCategory);
        
        try {
            // Act
            itemRepo.save(testItem);
            boolean exists = itemRepo.exists("TEST004");
            boolean notExists = itemRepo.exists("NONEXISTENT");
            
            // Assert
            assertTrue("Item should exist", exists);
            assertFalse("Non-existent item should not exist", notExists);
        } finally {
            // Cleanup
            itemRepo.delete("TEST004");
        }
    }
}