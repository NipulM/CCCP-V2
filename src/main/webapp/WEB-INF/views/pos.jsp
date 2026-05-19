<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.cb011999.cccp.domain.model.*, com.cb011999.cccp.domain.enums.*, com.cb011999.cccp.service.InventoryService, java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>SYOS - Point of Sale</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f0f2f5; }
        .navbar { background: #333; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .navbar h2 { margin: 0; }
        .navbar a { color: white; text-decoration: none; margin-left: 20px; }
        .container { max-width: 1400px; margin: 20px auto; padding: 0 20px; display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .panel { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h3 { margin-bottom: 15px; color: #333; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
        th, td { padding: 8px 12px; text-align: left; border-bottom: 1px solid #eee; }
        th { background: #f8f9fa; font-weight: bold; }
        .form-row { display: flex; gap: 10px; margin-bottom: 10px; }
        .form-row input, .form-row select { padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
        .form-row input[type="number"] { width: 80px; }
        .form-row select { flex: 1; }
        button, .btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; text-decoration: none; display: inline-block; }
        .btn-primary { background: #4CAF50; color: white; }
        .btn-danger { background: #f44336; color: white; }
        .btn-blue { background: #2196F3; color: white; }
        .btn-primary:hover { background: #45a049; }
        .btn-danger:hover { background: #da190b; }
        .success { background: #e8f5e9; color: #2e7d32; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
        .error { background: #ffebee; color: #c62828; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
        .bill-total { font-size: 20px; font-weight: bold; color: #333; margin: 15px 0; }
        .checkout-form { border-top: 2px solid #eee; padding-top: 15px; margin-top: 15px; }
        .checkout-form input { padding: 8px; border: 1px solid #ddd; border-radius: 4px; width: 150px; margin-right: 10px; }
        .completed-bill { background: #e8f5e9; border: 2px solid #4CAF50; padding: 20px; border-radius: 8px; }
    </style>
</head>
<body>
    <div class="navbar">
        <h2>SYOS - Point of Sale</h2>
        <div>
            <a href="dashboard">Dashboard</a>
            <a href="logout">Logout</a>
        </div>
    </div>

    <%-- Display success/error messages, then clear them from session --%>
    <%
        String posMessage = (String) session.getAttribute("posMessage");
        String posError = (String) session.getAttribute("posError");
        if (posMessage != null) {
            session.removeAttribute("posMessage");
    %>
        <div style="max-width:1400px;margin:20px auto;padding:0 20px;">
            <div class="success"><%= posMessage %></div>
        </div>
    <% } %>
    <% if (posError != null) {
            session.removeAttribute("posError");
    %>
        <div style="max-width:1400px;margin:20px auto;padding:0 20px;">
            <div class="error"><%= posError %></div>
        </div>
    <% } %>

    <%-- Show completed bill if one was just processed --%>
    <%
        Bill completedBill = (Bill) session.getAttribute("completedBill");
        if (completedBill != null) {
            session.removeAttribute("completedBill");
    %>
    <div style="max-width:1400px;margin:20px auto;padding:0 20px;">
        <div class="completed-bill">
            <h3>Bill #<%= completedBill.getSerialNumber() %> - PAID</h3>
            <table>
                <tr><th>Item</th><th>Qty</th><th>Price</th><th>Total</th></tr>
                <% for (BillItem bi : completedBill.getItems()) { %>
                <tr>
                    <td><%= bi.getName() %></td>
                    <td><%= bi.getQuantity() %></td>
                    <td>Rs. <%= String.format("%.2f", bi.getUnitPrice()) %></td>
                    <td>Rs. <%= String.format("%.2f", bi.getTotalPrice()) %></td>
                </tr>
                <% } %>
            </table>
            <p>Subtotal: Rs. <%= String.format("%.2f", completedBill.getTotalAmount()) %></p>
            <p>Discount: Rs. <%= String.format("%.2f", completedBill.getDiscount()) %></p>
            <p><strong>Total: Rs. <%= String.format("%.2f", completedBill.getFinalAmount()) %></strong></p>
            <p>Cash: Rs. <%= String.format("%.2f", completedBill.getCashTendered()) %></p>
            <p><strong>Change: Rs. <%= String.format("%.2f", completedBill.getChange()) %></strong></p>
        </div>
    </div>
    <% } %>

    <div class="container">
        <%-- LEFT PANEL: Available Items --%>
        <div class="panel">
            <h3>Available Items</h3>
            <table>
                <tr>
                    <th>Code</th>
                    <th>Name</th>
                    <th>Price</th>
                    <th>In Stock</th>
                </tr>
                <%
                    List<Item> allItems = (List<Item>) request.getAttribute("allItems");
                    InventoryService invService = (InventoryService) request.getAttribute("inventoryService");
                    if (allItems != null) {
                        for (Item item : allItems) {
                            int stock = invService.getTotalQuantity(item.getItemCode(), StoreType.PHYSICAL_STORE);
                %>
                <tr>
                    <td><%= item.getItemCode() %></td>
                    <td><%= item.getName() %></td>
                    <td>Rs. <%= String.format("%.2f", item.getUnitPrice()) %></td>
                    <td><%= stock %></td>
                </tr>
                <%      }
                    }
                %>
            </table>
        </div>

        <%-- RIGHT PANEL: Current Bill --%>
        <div class="panel">
            <%
                Bill currentBill = (Bill) request.getAttribute("currentBill");
                if (currentBill == null) {
            %>
                <%-- No active bill — show button to start one --%>
                <h3>No Active Bill</h3>
                <p>Start a new bill to begin processing a sale.</p>
                <form method="post" action="pos">
                    <input type="hidden" name="action" value="newBill">
                    <button type="submit" class="btn btn-primary">New Bill</button>
                </form>
            <% } else { %>
                <%-- Active bill — show items and controls --%>
                <h3>Bill #<%= currentBill.getSerialNumber() %></h3>

                <%-- Add item form --%>
                <form method="post" action="pos">
                    <input type="hidden" name="action" value="addItem">
                    <div class="form-row">
                        <select name="itemCode">
                            <% if (allItems != null) {
                                for (Item item : allItems) { %>
                                <option value="<%= item.getItemCode() %>">
                                    <%= item.getItemCode() %> - <%= item.getName() %>
                                </option>
                            <%  }
                               } %>
                        </select>
                        <input type="number" name="quantity" min="1" value="1" required>
                        <button type="submit" class="btn btn-blue">Add</button>
                    </div>
                </form>

                <%-- Bill items table --%>
                <% if (!currentBill.getItems().isEmpty()) { %>
                <table>
                    <tr><th>Item</th><th>Qty</th><th>Price</th><th>Total</th></tr>
                    <% for (BillItem bi : currentBill.getItems()) { %>
                    <tr>
                        <td><%= bi.getName() %></td>
                        <td><%= bi.getQuantity() %></td>
                        <td>Rs. <%= String.format("%.2f", bi.getUnitPrice()) %></td>
                        <td>Rs. <%= String.format("%.2f", bi.getTotalPrice()) %></td>
                    </tr>
                    <% } %>
                </table>

                <div class="bill-total">
                    Total: Rs. <%= String.format("%.2f", currentBill.getTotalAmount()) %>
                </div>

                <%-- Checkout form --%>
                <div class="checkout-form">
                    <form method="post" action="pos">
                        <input type="hidden" name="action" value="checkout">
                        <div class="form-row">
                            <input type="number" step="0.01" name="discount" placeholder="Discount (Rs.)" min="0">
                            <input type="number" step="0.01" name="cashTendered" placeholder="Cash Tendered (Rs.)" required min="0">
                            <button type="submit" class="btn btn-primary">Checkout</button>
                        </div>
                    </form>
                </div>
                <% } %>

                <%-- Cancel bill button --%>
                <form method="post" action="pos" style="margin-top: 10px;">
                    <input type="hidden" name="action" value="cancelBill">
                    <button type="submit" class="btn btn-danger">Cancel Bill</button>
                </form>
            <% } %>
        </div>
    </div>
</body>
<script>
    /**
     * Real-time stock polling.
     * 
     * Every 3 seconds, this function sends an HTTP request to /api/stock
     * asking for the latest stock levels. If the stock has changed since
     * the page loaded, the table updates automatically.
     * 
     * This is how Employee B sees changes made by Employee A without
     * refreshing the page — fulfilling the client-side concurrency requirement.
     * 
     * We use setInterval (runs repeatedly) rather than setTimeout (runs once)
     * because we want continuous updates as long as the page is open.
     */
    function pollStockUpdates() {
        fetch('api/stock?store=physical')
            .then(function(response) { return response.json(); })
            .then(function(items) {
                // Find the stock table on the left panel
                var table = document.querySelector('.container .panel:first-child table');
                if (!table) return;

                var rows = table.querySelectorAll('tr');
                // Skip header row (index 0)
                for (var i = 1; i < rows.length; i++) {
                    var cells = rows[i].querySelectorAll('td');
                    if (cells.length < 4) continue;

                    var code = cells[0].textContent.trim();

                    // Find matching item in the response
                    for (var j = 0; j < items.length; j++) {
                        if (items[j].code === code) {
                            var oldStock = cells[3].textContent.trim();
                            var newStock = items[j].stock.toString();

                            // Only update if changed — and briefly highlight the change
                            if (oldStock !== newStock) {
                                cells[3].textContent = newStock;
                                cells[3].style.backgroundColor = '#fff9c4';
                                // Remove highlight after 1 second
                                (function(cell) {
                                    setTimeout(function() {
                                        cell.style.backgroundColor = '';
                                    }, 1000);
                                })(cells[3]);
                            }
                            break;
                        }
                    }
                }
            })
            .catch(function(error) {
                console.log('Stock poll error:', error);
            });
    }

    // Poll every 3 seconds
    setInterval(pollStockUpdates, 3000);
</script>
</html>