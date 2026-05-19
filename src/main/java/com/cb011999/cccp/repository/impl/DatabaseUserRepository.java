package com.cb011999.cccp.repository.impl;

import com.cb011999.cccp.database.DatabaseConnection;
import com.cb011999.cccp.domain.model.User;
import com.cb011999.cccp.domain.model.OnlineCustomer;
import com.cb011999.cccp.domain.model.Employee;
import com.cb011999.cccp.repository.UserRepository;

import java.sql.*;
import java.util.*;

public class DatabaseUserRepository implements UserRepository {
    private static DatabaseUserRepository instance;
    private final DatabaseConnection dbConnection;
    
    private DatabaseUserRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public static synchronized DatabaseUserRepository getInstance() {
        if (instance == null) {
            instance = new DatabaseUserRepository();
        }
        return instance;
    }
    
    @Override
    public Optional<User> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = buildUserFromResultSet(rs);
                return Optional.of(user);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<OnlineCustomer> findCustomerByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ? AND user_type = 'ONLINE_CUSTOMER'";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                OnlineCustomer customer = (OnlineCustomer) buildUserFromResultSet(rs);
                return Optional.of(customer);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding customer by email: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Employee> findEmployeeByNumber(String employeeNumber) {
        String sql = "SELECT * FROM users WHERE employee_number = ? AND user_type = 'EMPLOYEE'";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, employeeNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Employee employee = (Employee) buildUserFromResultSet(rs);
                return Optional.of(employee);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding employee: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    @Override
    public void save(User user) {
        if (user instanceof OnlineCustomer) {
            saveOnlineCustomer((OnlineCustomer) user);
        } else if (user instanceof Employee) {
            saveEmployee((Employee) user);
        }
    }
    
    private void saveOnlineCustomer(OnlineCustomer customer) {
        String sql = "INSERT INTO users (id, name, contact_number, user_type, email, " +
                    "password_hash, delivery_address, is_registered) " +
                    "VALUES (?, ?, ?, 'ONLINE_CUSTOMER', ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "name = VALUES(name), " +
                    "contact_number = VALUES(contact_number), " +
                    "email = VALUES(email), " +
                    "password_hash = VALUES(password_hash), " +
                    "delivery_address = VALUES(delivery_address), " +
                    "is_registered = VALUES(is_registered)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customer.getId());
            pstmt.setString(2, customer.getName());
            pstmt.setString(3, customer.getContactNumber());
            pstmt.setString(4, customer.getEmail());
            pstmt.setString(5, customer.getPasswordHash());
            pstmt.setString(6, customer.getDeliveryAddress());
            pstmt.setBoolean(7, customer.isRegistered());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving online customer: " + e.getMessage());
        }
    }
    
    private void saveEmployee(Employee employee) {
        String sql = "INSERT INTO users (id, name, contact_number, user_type, " +
                    "employee_number, role) " +
                    "VALUES (?, ?, ?, 'EMPLOYEE', ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "name = VALUES(name), " +
                    "contact_number = VALUES(contact_number), " +
                    "employee_number = VALUES(employee_number), " +
                    "role = VALUES(role)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, employee.getId());
            pstmt.setString(2, employee.getName());
            pstmt.setString(3, employee.getContactNumber());
            pstmt.setString(4, employee.getEmployeeNumber());
            pstmt.setString(5, employee.getRole());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving employee: " + e.getMessage());
        }
    }
    
    @Override
    public List<OnlineCustomer> findAllCustomers() {
        List<OnlineCustomer> customers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE user_type = 'ONLINE_CUSTOMER'";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                OnlineCustomer customer = (OnlineCustomer) buildUserFromResultSet(rs);
                customers.add(customer);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all customers: " + e.getMessage());
        }
        
        return customers;
    }
    
    @Override
    public List<Employee> findAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE user_type = 'EMPLOYEE'";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Employee employee = (Employee) buildUserFromResultSet(rs);
                employees.add(employee);
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all employees: " + e.getMessage());
        }
        
        return employees;
    }
    
    @Override
    public boolean exists(String id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
        }
        
        return false;
    }
    
    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        
        return false;
    }
    
    private User buildUserFromResultSet(ResultSet rs) throws SQLException {
        String userType = rs.getString("user_type");
        
        if ("ONLINE_CUSTOMER".equals(userType)) {
            OnlineCustomer customer = new OnlineCustomer(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("contact_number"),
                rs.getString("email")
            );
            
            if (rs.getBoolean("is_registered")) {
                customer.register(
                    rs.getString("password_hash"),
                    rs.getString("delivery_address")
                );
            }
            
            return customer;
            
        } else if ("EMPLOYEE".equals(userType)) {
            return new Employee(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("contact_number"),
                rs.getString("employee_number"),
                rs.getString("role")
            );
        }
        
        throw new IllegalStateException("Unknown user type: " + userType);
    }
}