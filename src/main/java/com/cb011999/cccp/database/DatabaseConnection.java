package com.cb011999.cccp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/syos_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✓ Database connection established successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Database driver not found: " + e.getMessage());
            System.err.println("Make sure to add MySQL Connector JAR to build path");
        } catch (SQLException e) {
            System.err.println("✗ Failed to connect to database: " + e.getMessage());
        }
    }
    
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("✓ Database connection re-established");
            }
        } catch (SQLException e) {
            System.err.println("Error getting connection: " + e.getMessage());
        }
        return connection;
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public void initializeDatabase() {
        Statement stmt = null;
        try {
            // Ensure connection is valid
            Connection conn = getConnection();
            stmt = conn.createStatement();
            
            // Create Categories table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS categories (" +
                "id INT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "parent_category_id INT, " +
                "FOREIGN KEY (parent_category_id) REFERENCES categories(id)" +
                ")"
            );
            
            // Create Items table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS items (" +
                "item_code VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(200) NOT NULL, " +
                "unit_price DECIMAL(10,2) NOT NULL, " +
                "category_id INT, " +
                "FOREIGN KEY (category_id) REFERENCES categories(id)" +
                ")"
            );
            
            // Create Stock Batches table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS stock_batches (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "item_code VARCHAR(50) NOT NULL, " +
                "quantity INT NOT NULL, " +
                "purchase_date DATE NOT NULL, " +
                "expiry_date DATE NOT NULL, " +
                "location VARCHAR(20) NOT NULL, " +
                "FOREIGN KEY (item_code) REFERENCES items(item_code)" +
                ")"
            );
            
            // Create Bills table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS bills (" +
                "serial_number INT PRIMARY KEY, " +
                "bill_date DATETIME NOT NULL, " +
                "total_amount DECIMAL(10,2) NOT NULL, " +
                "discount DECIMAL(10,2) DEFAULT 0, " +
                "final_amount DECIMAL(10,2) NOT NULL, " +
                "cash_tendered DECIMAL(10,2), " +
                "change_amount DECIMAL(10,2), " +
                "transaction_type VARCHAR(20) NOT NULL, " +
                "store_type VARCHAR(20) NOT NULL, " +
                "status VARCHAR(20) NOT NULL, " +
                "payment_method VARCHAR(20)" +
                ")"
            );
            
            // Create Bill Items table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS bill_items (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "bill_serial_number INT NOT NULL, " +
                "item_code VARCHAR(50) NOT NULL, " +
                "item_name VARCHAR(200) NOT NULL, " +
                "quantity INT NOT NULL, " +
                "unit_price DECIMAL(10,2) NOT NULL, " +
                "total_price DECIMAL(10,2) NOT NULL, " +
                "FOREIGN KEY (bill_serial_number) REFERENCES bills(serial_number)" +
                ")"
            );
            
            // Create Users table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(200) NOT NULL, " +
                "contact_number VARCHAR(20), " +
                "user_type VARCHAR(20) NOT NULL, " +
                "email VARCHAR(100), " +
                "password_hash VARCHAR(255), " +
                "delivery_address TEXT, " +
                "employee_number VARCHAR(50), " +
                "role VARCHAR(50), " +
                "is_registered BOOLEAN DEFAULT FALSE" +
                ")"
            );
            
            System.out.println("✓ Database schema initialized successfully");
            
        } catch (SQLException e) {
            System.err.println("✗ Error initializing database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close only the statement, not the connection
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Error closing statement: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Executes a SQL script from a string.
     * Useful for running custom SQL commands.
     * 
     * @param sql SQL command to execute
     * @return true if successful
     */
    public boolean executeSQL(String sql) {
        Statement stmt = null;
        try {
            Connection conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Error executing SQL: " + e.getMessage());
            return false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Error closing statement: " + e.getMessage());
                }
            }
        }
    }
}