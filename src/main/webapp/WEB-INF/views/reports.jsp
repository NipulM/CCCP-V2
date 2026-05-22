<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.cb011999.cccp.service.report.model.*, java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>SYOS - Reports</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f0f2f5; }
        .navbar { background: #333; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .navbar h2 { margin: 0; }
        .navbar a { color: white; text-decoration: none; margin-left: 20px; }
        .container { max-width: 1200px; margin: 20px auto; padding: 0 20px; }
        .panel { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); margin-bottom: 20px; }
        h3 { margin-bottom: 15px; color: #333; }
        .tabs { display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; }
        .tabs a { padding: 10px 16px; border-radius: 4px; text-decoration: none; color: #333; background: #e0e0e0; font-size: 14px; }
        .tabs a.active { background: #4CAF50; color: white; }
        .filters { display: flex; gap: 10px; margin-bottom: 20px; align-items: center; }
        .filters select { padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
        .filters button { padding: 8px 16px; background: #2196F3; color: white; border: none; border-radius: 4px; cursor: pointer; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eee; }
        th { background: #f8f9fa; font-weight: bold; }
        .summary { background: #f8f9fa; padding: 15px; border-radius: 4px; margin-top: 15px; }
        .summary p { margin-bottom: 5px; }
    </style>
</head>
<body>
    <div class="navbar">
        <h2>Reports</h2>
        <div>
            <a href="dashboard">Dashboard</a>
            <a href="logout">Logout</a>
        </div>
    </div>

    <div class="container">
        <%
            String reportType = (String) request.getAttribute("reportType");
            String txFilter = (String) request.getAttribute("transactionFilter");
            String storeFilter = (String) request.getAttribute("storeFilter");
            if (txFilter == null) txFilter = "";
            if (storeFilter == null) storeFilter = "";
        %>

        <%-- Report type tabs --%>
        <div class="tabs">
            <a href="reports?type=daily" class="<%= "daily".equals(reportType) ? "active" : "" %>">Daily Sales</a>
            <a href="reports?type=bill" class="<%= "bill".equals(reportType) ? "active" : "" %>">Bill Report</a>
            <a href="reports?type=reshelf" class="<%= "reshelf".equals(reportType) ? "active" : "" %>">Reshelf</a>
            <a href="reports?type=reorder" class="<%= "reorder".equals(reportType) ? "active" : "" %>">Reorder</a>
            <a href="reports?type=stock" class="<%= "stock".equals(reportType) ? "active" : "" %>">Stock</a>
        </div>

        <%-- Filters — allows filtering by transaction type and store type --%>
        <div class="panel">
            <form method="get" action="reports">
                <input type="hidden" name="type" value="<%= reportType %>">
                <div class="filters">
                    <label>Transaction:</label>
                    <select name="transactionType">
                        <option value="">All</option>
                        <option value="OVER_THE_COUNTER" <%= "OVER_THE_COUNTER".equals(txFilter) ? "selected" : "" %>>Over the Counter</option>
                        <option value="ONLINE" <%= "ONLINE".equals(txFilter) ? "selected" : "" %>>Online</option>
                    </select>
                    <label>Store:</label>
                    <select name="storeType">
                        <option value="">All</option>
                        <option value="PHYSICAL_STORE" <%= "PHYSICAL_STORE".equals(storeFilter) ? "selected" : "" %>>Physical Store</option>
                        <option value="ONLINE_STORE" <%= "ONLINE_STORE".equals(storeFilter) ? "selected" : "" %>>Online Store</option>
                    </select>
                    <button type="submit">Apply Filters</button>
                </div>
            </form>
        </div>

        <div class="panel">

        <%-- DAILY SALES REPORT --%>
        <% if ("daily".equals(reportType)) {
            DailySalesReport dailyReport = (DailySalesReport) request.getAttribute("dailyReport");
            if (dailyReport != null) {
        %>
            <h3>Daily Sales Report - <%= dailyReport.getReportDate() %></h3>
            <table>
                <tr><th>Code</th><th>Item Name</th><th>Quantity Sold</th><th>Revenue</th></tr>
                <% for (DailySalesReport.SalesItem item : dailyReport.getItems()) { %>
                <tr>
                    <td><%= item.getItemCode() %></td>
                    <td><%= item.getItemName() %></td>
                    <td><%= item.getTotalQuantity() %></td>
                    <td>Rs. <%= String.format("%.2f", item.getTotalRevenue()) %></td>
                </tr>
                <% } %>
            </table>
            <div class="summary">
                <p><strong>Total Revenue: Rs. <%= String.format("%.2f", dailyReport.getTotalRevenue()) %></strong></p>
            </div>
        <% } } %>
        
        <%-- BILL REPORT --%>
        <% if ("bill".equals(reportType)) {
            BillReport billReport = (BillReport) request.getAttribute("billReport");
            if (billReport != null) {
        %>
            <h3>Bill Report</h3>
            <table>
                <tr>
                    <th>Serial #</th><th>Date</th><th>Amount</th>
                    <th>Discount</th><th>Final</th><th>Type</th><th>Store</th>
                </tr>
                <% for (BillReport.BillSummary bill : billReport.getBills()) { %>
                <tr>
                    <td><%= bill.getSerialNumber() %></td>
                    <td><%= bill.getBillDate().toString().substring(0, 16) %></td>
                    <td>Rs. <%= String.format("%.2f", bill.getTotalAmount()) %></td>
                    <td>Rs. <%= String.format("%.2f", bill.getDiscount()) %></td>
                    <td>Rs. <%= String.format("%.2f", bill.getFinalAmount()) %></td>
                    <td><%= bill.getTransactionType().getDisplayName() %></td>
                    <td><%= bill.getStoreType().getDisplayName() %></td>
                </tr>
                <% } %>
            </table>
            <div class="summary">
                <p>Total Bills: <%= billReport.getTotalBills() %></p>
                <p><strong>Total Revenue: Rs. <%= String.format("%.2f", billReport.getTotalRevenue()) %></strong></p>
            </div>
        <% } } %>

        <%-- RESHELF REPORT --%>
        <% if ("reshelf".equals(reportType)) {
            ReshelfReport reshelfReport = (ReshelfReport) request.getAttribute("reshelfReport");
            if (reshelfReport != null) {
        %>
            <h3>Reshelf Report (Expiring within 3 days)</h3>
            <table>
                <tr><th>Code</th><th>Item Name</th><th>Quantity</th><th>Reason</th></tr>
                <% for (ReshelfReport.ReshelfItem item : reshelfReport.getItems()) { %>
                <tr>
                    <td><%= item.getItemCode() %></td>
                    <td><%= item.getItemName() %></td>
                    <td><%= item.getQuantity() %></td>
                    <td><%= item.getReason() %></td>
                </tr>
                <% } %>
            </table>
            <div class="summary">
                <p>Total Items: <%= reshelfReport.getTotalItems() %></p>
                <p>Total Quantity: <%= reshelfReport.getTotalQuantity() %></p>
            </div>
        <% } } %>

        <%-- REORDER REPORT --%>
        <% if ("reorder".equals(reportType)) {
            ReorderReport reorderReport = (ReorderReport) request.getAttribute("reorderReport");
            if (reorderReport != null) {
        %>
            <h3>Reorder Levels Report (Below 50 units)</h3>
            <table>
                <tr><th>Code</th><th>Item Name</th><th>Current Qty</th><th>Suggested Order</th></tr>
                <% for (ReorderReport.ReorderItem item : reorderReport.getItems()) { %>
                <tr>
                    <td><%= item.getItemCode() %></td>
                    <td><%= item.getItemName() %></td>
                    <td><%= item.getCurrentQuantity() %></td>
                    <td><%= item.getSuggestedOrderQuantity() %></td>
                </tr>
                <% } %>
            </table>
            <div class="summary">
                <p>Total Items Needing Reorder: <%= reorderReport.getTotalItemsNeedingReorder() %></p>
            </div>
        <% } } %>

        <%-- STOCK REPORT --%>
        <% if ("stock".equals(reportType)) {
            StockReport stockReport = (StockReport) request.getAttribute("stockReport");
            if (stockReport != null) {
        %>
            <h3>Stock Report (Warehouse)</h3>
            <table>
                <tr><th>Code</th><th>Item Name</th><th>Quantity</th><th>Purchase Date</th><th>Expiry Date</th></tr>
                <% for (StockReport.StockBatchInfo batch : stockReport.getBatches()) { %>
                <tr>
                    <td><%= batch.getItemCode() %></td>
                    <td><%= batch.getItemName() %></td>
                    <td><%= batch.getQuantity() %></td>
                    <td><%= batch.getPurchaseDate() %></td>
                    <td><%= batch.getExpiryDate() %></td>
                </tr>
                <% } %>
            </table>
            <div class="summary">
                <p>Total Batches: <%= stockReport.getTotalBatches() %></p>
                <p>Total Quantity: <%= stockReport.getTotalQuantity() %></p>
            </div>
        <% } } %>

        </div>
    </div>
</body>
</html>