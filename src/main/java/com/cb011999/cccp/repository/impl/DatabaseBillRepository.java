package com.cb011999.cccp.repository.impl;

import com.cb011999.cccp.database.DatabaseConnection;
import com.cb011999.cccp.domain.enums.BillStatus;
import com.cb011999.cccp.domain.enums.PaymentMethod;
import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.domain.model.Bill;
import com.cb011999.cccp.domain.model.BillItem;
import com.cb011999.cccp.repository.BillRepository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseBillRepository implements BillRepository {
    private static DatabaseBillRepository instance;
    private final DatabaseConnection dbConnection;
    
    private DatabaseBillRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    public static synchronized DatabaseBillRepository getInstance() {
        if (instance == null) {
            instance = new DatabaseBillRepository();
        }
        return instance;
    }
    
    @Override
    public void save(Bill bill) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            if (conn == null) {
                System.err.println("Error saving bill: no database connection");
                return;
            }
            conn.setAutoCommit(false);
            try {
                saveBillAndItems(conn, bill);
                conn.commit();
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error saving bill: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Saves bill row then replaces all bill_items for this bill, using the same connection. */
    private void saveBillAndItems(Connection conn, Bill bill) throws SQLException {
        String billSql = "INSERT INTO bills (serial_number, bill_date, total_amount, discount, " +
                        "final_amount, cash_tendered, change_amount, transaction_type, " +
                        "store_type, status, payment_method) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "total_amount = VALUES(total_amount), " +
                        "discount = VALUES(discount), " +
                        "final_amount = VALUES(final_amount), " +
                        "cash_tendered = VALUES(cash_tendered), " +
                        "change_amount = VALUES(change_amount), " +
                        "status = VALUES(status), " +
                        "payment_method = VALUES(payment_method)";
        try (PreparedStatement pstmt = conn.prepareStatement(billSql)) {
            pstmt.setInt(1, bill.getSerialNumber());
            pstmt.setTimestamp(2, Timestamp.valueOf(bill.getBillDate()));
            pstmt.setDouble(3, bill.getTotalAmount());
            pstmt.setDouble(4, bill.getDiscount());
            pstmt.setDouble(5, bill.getFinalAmount());
            pstmt.setDouble(6, bill.getCashTendered());
            pstmt.setDouble(7, bill.getChange());
            pstmt.setString(8, bill.getTransactionType().name());
            pstmt.setString(9, bill.getStoreType().name());
            pstmt.setString(10, bill.getStatus().name());
            pstmt.setString(11, bill.getPaymentMethodType() != null ? bill.getPaymentMethodType().name() : null);
            pstmt.executeUpdate();
        }

        int serial = bill.getSerialNumber();
        String deleteSql = "DELETE FROM bill_items WHERE bill_serial_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setInt(1, serial);
            pstmt.executeUpdate();
        }

        String itemSql = "INSERT INTO bill_items (bill_serial_number, item_code, item_name, " +
                        "quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
            for (BillItem item : bill.getItems()) {
                pstmt.setInt(1, serial);
                pstmt.setString(2, item.getItemCode());
                pstmt.setString(3, item.getName());
                pstmt.setInt(4, item.getQuantity());
                pstmt.setDouble(5, item.getUnitPrice());
                pstmt.setDouble(6, item.getTotalPrice());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
    
    @Override
    public Optional<Bill> findBySerialNumber(int serialNumber) {
        String sql = "SELECT * FROM bills WHERE serial_number = ?";
        Bill bill = null;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, serialNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bill = buildBillFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding bill: " + e.getMessage());
            return Optional.empty();
        }
        if (bill != null) {
            try {
                loadBillItems(bill);
            } catch (SQLException e) {
                System.err.println("Error loading items for bill " + bill.getSerialNumber() + ": " + e.getMessage());
            }
            return Optional.of(bill);
        }
        return Optional.empty();
    }
    
    @Override
    public List<Bill> findByDate(LocalDate date) {
        String sql = "SELECT * FROM bills WHERE DATE(bill_date) = ?";
        return findBillsBySql(sql, date);
    }
    
    @Override
    public List<Bill> findByTransactionType(TransactionType transactionType) {
        String sql = "SELECT * FROM bills WHERE transaction_type = ?";
        return findBillsBySql(sql, transactionType.name());
    }
    
    @Override
    public List<Bill> findByStoreType(StoreType storeType) {
        String sql = "SELECT * FROM bills WHERE store_type = ?";
        return findBillsBySql(sql, storeType.name());
    }
    
    @Override
    public List<Bill> findByFilters(LocalDate date, TransactionType transactionType, 
                                     StoreType storeType) {
        StringBuilder sql = new StringBuilder("SELECT * FROM bills WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (date != null) {
            sql.append(" AND DATE(bill_date) = ?");
            params.add(date);
        }
        if (transactionType != null) {
            sql.append(" AND transaction_type = ?");
            params.add(transactionType.name());
        }
        if (storeType != null) {
            sql.append(" AND store_type = ?");
            params.add(storeType.name());
        }
        
        return findBillsBySql(sql.toString(), params.toArray());
    }
    
    @Override
    public List<Bill> findAll() {
        String sql = "SELECT * FROM bills ORDER BY serial_number";
        return findBillsBySql(sql);
    }
    
    private List<Bill> findBillsBySql(String sql, Object... params) {
        List<Bill> bills = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            ResultSet rs = pstmt.executeQuery();
            // Consume full ResultSet first; do NOT open another Statement (loadBillItems)
            // here or the driver may close this ResultSet and we get "ResultSet closed".
            while (rs.next()) {
                bills.add(buildBillFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding bills: " + e.getMessage());
            return bills;
        }
        // Now load items for each bill (separate Statements are safe here)
        for (Bill bill : bills) {
            try {
                loadBillItems(bill);
            } catch (SQLException e) {
                System.err.println("Error loading items for bill " + bill.getSerialNumber() + ": " + e.getMessage());
            }
        }
        return bills;
    }
    
    private Bill buildBillFromResultSet(ResultSet rs) throws SQLException {
        int serialNumber = rs.getInt("serial_number");
        Timestamp billDateTs = rs.getTimestamp("bill_date");
        LocalDateTime billDate = billDateTs != null ? billDateTs.toLocalDateTime() : LocalDateTime.now();
        TransactionType transactionType = TransactionType.valueOf(rs.getString("transaction_type"));
        StoreType storeType = StoreType.valueOf(rs.getString("store_type"));
        double discount = rs.getDouble("discount");
        BillStatus status = BillStatus.valueOf(rs.getString("status"));
        double cashTendered = rs.getDouble("cash_tendered");
        double change = rs.getDouble("change_amount");
        Bill bill = Bill.fromPersisted(serialNumber, billDate, transactionType, storeType,
                0.0, discount, status, cashTendered, change);
        try {
            String paymentMethodStr = rs.getString("payment_method");
            if (paymentMethodStr != null && !paymentMethodStr.isEmpty()) {
                bill.setPaymentMethodType(PaymentMethod.valueOf(paymentMethodStr));
            }
        } catch (SQLException ignored) {
            // Column may not exist in older DBs
        }
        return bill;
    }

    private void loadBillItems(Bill bill) throws SQLException {
        String sql = "SELECT * FROM bill_items WHERE bill_serial_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bill.getSerialNumber());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String itemName = rs.getString("item_name");
                String itemCode = rs.getString("item_code");
                int quantity = rs.getInt("quantity");
                double unitPrice = rs.getDouble("unit_price");
                BillItem item = new BillItem(itemName, itemCode, quantity, unitPrice);
                bill.addLoadedItem(item);
            }
        }
    }
}