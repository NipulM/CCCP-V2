<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.cb011999.cccp.domain.model.Item, java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>SYOS - Stock Management</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f0f2f5; }
        .navbar { background: #333; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .navbar h2 { margin: 0; }
        .navbar a { color: white; text-decoration: none; margin-left: 20px; }
        .container { max-width: 1000px; margin: 20px auto; padding: 0 20px; }
        .panel { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); margin-bottom: 20px; }
        h3 { margin-bottom: 15px; color: #333; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; color: #555; font-weight: bold; }
        .form-group select, .form-group input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
        button { padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; color: white; }
        .btn-primary { background: #4CAF50; }
        .btn-blue { background: #2196F3; }
        .success { background: #e8f5e9; color: #2e7d32; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
        .error { background: #ffebee; color: #c62828; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
    </style>
</head>
<body>
    <div class="navbar">
        <h2>SYOS - Stock Management</h2>
        <div>
            <a href="dashboard">Dashboard</a>
            <a href="logout">Logout</a>
        </div>
    </div>

    <div class="container">
        <%
            String stockMessage = (String) session.getAttribute("stockMessage");
            String stockError = (String) session.getAttribute("stockError");
            if (stockMessage != null) { session.removeAttribute("stockMessage"); %>
                <div class="success"><%= stockMessage %></div>
        <% } %>
        <% if (stockError != null) { session.removeAttribute("stockError"); %>
                <div class="error"><%= stockError %></div>
        <% } %>

        <%
            List<Item> allItems = (List<Item>) request.getAttribute("allItems");
        %>

        <%-- Add Stock to Warehouse --%>
        <div class="panel">
            <h3>Add Stock to Warehouse</h3>
            <form method="post" action="stock">
                <input type="hidden" name="action" value="addStock">
                <div class="form-group">
                    <label>Item:</label>
                    <select name="itemCode" required>
                        <% if (allItems != null) {
                            for (Item item : allItems) { %>
                            <option value="<%= item.getItemCode() %>">
                                <%= item.getItemCode() %> - <%= item.getName() %>
                            </option>
                        <% } } %>
                    </select>
                </div>
                <div class="form-group">
                    <label>Quantity:</label>
                    <input type="number" name="quantity" min="1" required>
                </div>
                <div class="form-group">
                    <label>Days Until Expiry:</label>
                    <input type="number" name="daysUntilExpiry" min="1" required>
                </div>
                <button type="submit" class="btn-primary">Add to Warehouse</button>
            </form>
        </div>

        <%-- Restock Shelves from Warehouse --%>
        <div class="panel">
            <h3>Restock from Warehouse</h3>
            <form method="post" action="stock">
                <input type="hidden" name="action" value="restock">
                <div class="form-group">
                    <label>Item:</label>
                    <select name="itemCode" required>
                        <% if (allItems != null) {
                            for (Item item : allItems) { %>
                            <option value="<%= item.getItemCode() %>">
                                <%= item.getItemCode() %> - <%= item.getName() %>
                            </option>
                        <% } } %>
                    </select>
                </div>
                <div class="form-group">
                    <label>Quantity:</label>
                    <input type="number" name="quantity" min="1" required>
                </div>
                <div class="form-group">
                    <label>Restock To:</label>
                    <select name="target">
                        <option value="shelf">Physical Store Shelf</option>
                        <option value="online">Online Store</option>
                    </select>
                </div>
                <button type="submit" class="btn-blue">Restock</button>
            </form>
        </div>
    </div>
</body>
</html>