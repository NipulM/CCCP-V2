package com.cb011999.cccp.repository.impl;

import com.cb011999.cccp.database.DatabaseConnection;
import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.model.StockBatch;
import com.cb011999.cccp.repository.InventoryRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInventoryRepository implements InventoryRepository {
    private static DatabaseInventoryRepository instance;
    private final DatabaseConnection dbConnection;
    
    private DatabaseInventoryRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public static synchronized DatabaseInventoryRepository getInstance() {
        if (instance == null) {
            instance = new DatabaseInventoryRepository();
        }
        return instance;
    }
    
    @Override
    public void addToStore(StockBatch batch) {
        addBatchWithLocation(batch, "STORE");
    }
    
    @Override
    public void addToInventory(StockBatch batch, StoreType storeType) {
        String location = storeType == StoreType.PHYSICAL_STORE ? "SHELF" : "ONLINE";
        addBatchWithLocation(batch, location);
    }
    
    private void addBatchWithLocation(StockBatch batch, String location) {
        String sql = "INSERT INTO stock_batches (item_code, quantity, purchase_date, " +
                    "expiry_date, location) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, batch.getItemCode());
            pstmt.setInt(2, batch.getQuantity());
            pstmt.setDate(3, Date.valueOf(batch.getPurchaseDate()));
            pstmt.setDate(4, Date.valueOf(batch.getExpiryDate()));
            pstmt.setString(5, location);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error adding stock batch: " + e.getMessage());
        }
    }
    
    @Override
    public List<StockBatch> getStoreStock(String itemCode) {
        return getStockByLocation(itemCode, "STORE");
    }
    
    @Override
    public List<StockBatch> getShelfStock(String itemCode) {
        return getStockByLocation(itemCode, "SHELF");
    }
    
    @Override
    public List<StockBatch> getOnlineStock(String itemCode) {
        return getStockByLocation(itemCode, "ONLINE");
    }
    
    @Override
    public List<StockBatch> getInventory(String itemCode, StoreType storeType) {
        String location = storeType == StoreType.PHYSICAL_STORE ? "SHELF" : "ONLINE";
        return getStockByLocation(itemCode, location);
    }
    
    private List<StockBatch> getStockByLocation(String itemCode, String location) {
        List<StockBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM stock_batches WHERE item_code = ? AND location = ? " +
                    "ORDER BY expiry_date, purchase_date";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, itemCode);
            pstmt.setString(2, location);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                StockBatch batch = buildBatchFromResultSet(rs);
                batches.add(batch);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting stock: " + e.getMessage());
        }
        
        return batches;
    }
    
    @Override
    public List<StockBatch> getAllStoreStock() {
        return getAllStockByLocation("STORE");
    }
    
    @Override
    public List<StockBatch> getAllShelfStock() {
        return getAllStockByLocation("SHELF");
    }
    
    @Override
    public List<StockBatch> getAllOnlineStock() {
        return getAllStockByLocation("ONLINE");
    }
    
    private List<StockBatch> getAllStockByLocation(String location) {
        List<StockBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM stock_batches WHERE location = ? " +
                    "ORDER BY item_code, expiry_date";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, location);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                StockBatch batch = buildBatchFromResultSet(rs);
                batches.add(batch);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all stock: " + e.getMessage());
        }
        
        return batches;
    }
    
    @Override
    public int getTotalQuantity(String itemCode, StoreType storeType) {
        String location = storeType == StoreType.PHYSICAL_STORE ? "SHELF" : "ONLINE";
        String sql = "SELECT SUM(quantity) FROM stock_batches " +
                    "WHERE item_code = ? AND location = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, itemCode);
            pstmt.setString(2, location);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total quantity: " + e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public void updateStoreBatch(StockBatch batch) {
        updateBatchInDb(batch, "STORE");
    }

    @Override
    public void updateInventoryBatch(StockBatch batch, StoreType storeType) {
        String location = storeType == StoreType.PHYSICAL_STORE ? "SHELF" : "ONLINE";
        updateBatchInDb(batch, location);
    }

    private void updateBatchInDb(StockBatch batch, String location) {
        try (Connection conn = dbConnection.getConnection()) {
            if (batch.getId() != null) {
                // Update by primary key so we always hit the exact row we read
                String sql = "UPDATE stock_batches SET quantity = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, batch.getQuantity());
                    pstmt.setInt(2, batch.getId());
                    pstmt.executeUpdate();
                }
            } else {
                // Fallback when batch was created in app (no id)
                String sql = "UPDATE stock_batches SET quantity = ? " +
                            "WHERE item_code = ? AND purchase_date = ? AND expiry_date = ? AND location = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, batch.getQuantity());
                    pstmt.setString(2, batch.getItemCode());
                    pstmt.setDate(3, Date.valueOf(batch.getPurchaseDate()));
                    pstmt.setDate(4, Date.valueOf(batch.getExpiryDate()));
                    pstmt.setString(5, location);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating batch: " + e.getMessage());
        }
    }
    
    private StockBatch buildBatchFromResultSet(ResultSet rs) throws SQLException {
        StockBatch batch = new StockBatch(
            rs.getString("item_code"),
            rs.getInt("quantity"),
            rs.getDate("purchase_date").toLocalDate(),
            rs.getDate("expiry_date").toLocalDate()
        );
        batch.setId(rs.getInt("id"));
        return batch;
    }
}