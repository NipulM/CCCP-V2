<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.cb011999.cccp.domain.model.StockBatch, java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>SYOS - Inventory</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f0f2f5; }
        .navbar { background: #333; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .navbar h2 { margin: 0; }
        .navbar a { color: white; text-decoration: none; margin-left: 20px; }
        .container { max-width: 1200px; margin: 20px auto; padding: 0 20px; }
        .panel { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h3 { margin-bottom: 15px; color: #333; }
        .tabs { display: flex; gap: 10px; margin-bottom: 20px; }
        .tabs a { padding: 10px 20px; border-radius: 4px; text-decoration: none; color: #333; background: #e0e0e0; }
        .tabs a.active { background: #4CAF50; color: white; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; }
        th { background: #f8f9fa; font-weight: bold; }
        .low-stock { background: #fff3e0; }
        .expired { background: #ffebee; }
    </style>
</head>
<body>
    <div class="navbar">
        <h2>SYOS - Inventory</h2>
        <div>
            <a href="dashboard">Dashboard</a>
            <a href="logout">Logout</a>
        </div>
    </div>

    <div class="container">
        <div class="panel">
            <%
                String viewType = (String) request.getAttribute("viewType");
            %>
            <div class="tabs">
                <a href="inventory?view=shelf" class="<%= "shelf".equals(viewType) ? "active" : "" %>">Shelf</a>
                <a href="inventory?view=online" class="<%= "online".equals(viewType) ? "active" : "" %>">Online</a>
                <a href="inventory?view=warehouse" class="<%= "warehouse".equals(viewType) ? "active" : "" %>">Warehouse</a>
            </div>

            <h3><%= request.getAttribute("title") %></h3>

            <table>
                <tr>
                    <th>Item Code</th>
                    <th>Quantity</th>
                    <th>Purchase Date</th>
                    <th>Expiry Date</th>
                    <th>Days to Expiry</th>
                </tr>
                <%
                    List<StockBatch> batches = (List<StockBatch>) request.getAttribute("batches");
                    if (batches != null && !batches.isEmpty()) {
                        for (StockBatch batch : batches) {
                            String rowClass = "";
                            if (batch.getDaysUntilExpiry() <= 0) rowClass = "expired";
                            else if (batch.getQuantity() < 50) rowClass = "low-stock";
                %>
                <tr class="<%= rowClass %>">
                    <td><%= batch.getItemCode() %></td>
                    <td><%= batch.getQuantity() %></td>
                    <td><%= batch.getPurchaseDate() %></td>
                    <td><%= batch.getExpiryDate() %></td>
                    <td><%= batch.getDaysUntilExpiry() %></td>
                </tr>
                <%      }
                    } else {
                %>
                <tr><td colspan="5" style="text-align:center; color:#999;">No stock found</td></tr>
                <% } %>
            </table>
        </div>
    </div>
</body>
</html>