<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.cb011999.cccp.service.report.model.BillReport" %>
<!DOCTYPE html>
<html>
<head>
    <title>SYOS - My Orders</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f0f2f5; }
        .navbar { background: #2196F3; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .navbar h2 { margin: 0; }
        .navbar a { color: white; text-decoration: none; margin-left: 20px; }
        .container { max-width: 1000px; margin: 20px auto; padding: 0 20px; }
        .panel { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h3 { margin-bottom: 15px; color: #333; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; }
        th { background: #f8f9fa; font-weight: bold; }
        .summary { background: #f8f9fa; padding: 15px; border-radius: 4px; margin-top: 15px; }
    </style>
</head>
<body>
    <div class="navbar">
        <h2>SYOS - My Orders</h2>
        <div>
            <a href="shop">Continue Shopping</a>
            <a href="dashboard">Dashboard</a>
            <a href="logout">Logout</a>
        </div>
    </div>

    <div class="container">
        <div class="panel">
            <h3>Order History</h3>
            <%
                BillReport billReport = (BillReport) request.getAttribute("billReport");
                if (billReport != null && billReport.getTotalBills() > 0) {
            %>
            <table>
                <tr>
                    <th>Order #</th>
                    <th>Date</th>
                    <th>Items</th>
                    <th>Total</th>
                </tr>
                <% for (BillReport.BillSummary bill : billReport.getBills()) { %>
                <tr>
                    <td><%= bill.getSerialNumber() %></td>
                    <td><%= bill.getBillDate().toString().substring(0, 16) %></td>
                    <td><%= bill.getItemCount() %> items</td>
                    <td>Rs. <%= String.format("%.2f", bill.getFinalAmount()) %></td>
                </tr>
                <% } %>
            </table>
            <div class="summary">
                <p>Total Orders: <%= billReport.getTotalBills() %></p>
                <p><strong>Total Spent: Rs. <%= String.format("%.2f", billReport.getTotalRevenue()) %></strong></p>
            </div>
            <% } else { %>
                <p style="color:#999; text-align:center; padding:30px 0;">No orders yet. Start shopping!</p>
            <% } %>
        </div>
    </div>
</body>
</html>