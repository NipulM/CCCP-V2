package com.cb011999.cccp.repository.impl;

import com.cb011999.cccp.database.DatabaseConnection;
import com.cb011999.cccp.domain.model.Category;
import com.cb011999.cccp.domain.model.Item;
import com.cb011999.cccp.repository.ItemRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseItemRepository implements ItemRepository {
    private static DatabaseItemRepository instance;
    private final DatabaseConnection dbConnection;
    
    private DatabaseItemRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public static synchronized DatabaseItemRepository getInstance() {
        if (instance == null) {
            instance = new DatabaseItemRepository();
        }
        return instance;
    }
    
    @Override
    public Optional<Item> findByCode(String itemCode) {
        String sql = "SELECT i.item_code, i.name, i.unit_price, " +
                    "c.id as cat_id, c.name as cat_name, c.parent_category_id " +
                    "FROM items i " +
                    "LEFT JOIN categories c ON i.category_id = c.id " +
                    "WHERE i.item_code = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, itemCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Category category = null;
                if (rs.getInt("cat_id") != 0) {
                    category = new Category(
                        rs.getInt("cat_id"),
                        rs.getString("cat_name")
                    );
                }
                
                Item item = new Item(
                    rs.getString("item_code"),
                    rs.getString("name"),
                    rs.getDouble("unit_price"),
                    category
                );
                
                return Optional.of(item);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding item: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Item> findByCategory(int categoryId) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT i.item_code, i.name, i.unit_price, " +
                    "c.id as cat_id, c.name as cat_name " +
                    "FROM items i " +
                    "LEFT JOIN categories c ON i.category_id = c.id " +
                    "WHERE i.category_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Category category = new Category(
                    rs.getInt("cat_id"),
                    rs.getString("cat_name")
                );
                
                Item item = new Item(
                    rs.getString("item_code"),
                    rs.getString("name"),
                    rs.getDouble("unit_price"),
                    category
                );
                
                items.add(item);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding items by category: " + e.getMessage());
        }
        
        return items;
    }
    
    @Override
    public void save(Item item) {
        String sql = "INSERT INTO items (item_code, name, unit_price, category_id) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "name = VALUES(name), " +
                    "unit_price = VALUES(unit_price), " +
                    "category_id = VALUES(category_id)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, item.getItemCode());
            pstmt.setString(2, item.getName());
            pstmt.setDouble(3, item.getUnitPrice());
            
            if (item.getCategory() != null) {
                pstmt.setInt(4, item.getCategory().getId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving item: " + e.getMessage());
        }
    }
    
    @Override
    public boolean delete(String itemCode) {
        String sql = "DELETE FROM items WHERE item_code = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, itemCode);
            int rowsAffected = pstmt.executeUpdate();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting item: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT i.item_code, i.name, i.unit_price, " +
                    "c.id as cat_id, c.name as cat_name " +
                    "FROM items i " +
                    "LEFT JOIN categories c ON i.category_id = c.id";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Category category = null;
                if (rs.getInt("cat_id") != 0) {
                    category = new Category(
                        rs.getInt("cat_id"),
                        rs.getString("cat_name")
                    );
                }
                
                Item item = new Item(
                    rs.getString("item_code"),
                    rs.getString("name"),
                    rs.getDouble("unit_price"),
                    category
                );
                
                items.add(item);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all items: " + e.getMessage());
        }
        
        return items;
    }
    
    @Override
    public boolean exists(String itemCode) {
        String sql = "SELECT COUNT(*) FROM items WHERE item_code = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, itemCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking item existence: " + e.getMessage());
        }
        
        return false;
    }
}