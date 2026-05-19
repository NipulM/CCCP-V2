<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.cb011999.cccp.domain.model.*, com.cb011999.cccp.domain.enums.*, com.cb011999.cccp.service.InventoryService, com.cb011999.cccp.service.PointOfSaleService, java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>SYOS - Online Shop</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f0f2f5; }
        .navbar { background: #2196F3; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .navbar h2 { margin: 0; }
        .navbar a { color: white; text-decoration: none; margin-left: 20px; }
        .container { max-width: 1400px; margin: 20px auto; padding: 0 20px; display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }
        .panel { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h3 { margin-bottom: 15px; color: #333; }
        .items-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 15px; }
        .item-card { border: 1px solid #eee; border-radius: 8px; padding: 15px; text-align: center; }
        .item-card h4 { color: #333; margin-bottom: 5px; }
        .item-card .price { color: #2196F3; font-size: 18px; font-weight: bold; margin: 8px 0; }
        .item-card .stock { color: #666; font-size: 13px; margin-bottom: 10px; }
        .item-card .stock.low { color: #f44336; }
        .item-card form { display: flex; gap: 5px; justify-content: center; }
        .item-card input[type="number"] { width: 60px; padding: 6px; border: 1px solid #ddd; border-radius: 4px; text-align: center; }
        .item-card button { padding: 6px 12px; background: #2196F3; color: white; border: none; border-radius: 4px; cursor: pointer; }
        .item-card button:hover { background: #1976D2; }
        .item-card .out-of-stock { color: #f44336; font-weight: bold; }
        table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
        th, td { padding: 8px; text-align: left; border-bottom: 1px solid #eee; font-size: 14px; }
        th { background: #f8f9fa; }
        .cart-total { font-size: 20px; font-weight: bold; margin: 15px 0; color: #333; }
        .btn { padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; color: white; width: 100%; margin-bottom: 10px; }
        .btn-checkout { background: #4CAF50; }
        .btn-checkout:hover { background: #45a049; }
        .btn-clear { background: #f44336; }
        .btn-clear:hover { background: #da190b; }
        .success { background: #e8f5e9; color: #2e7d32; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
        .error { background: #ffebee; color: #c62828; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
        .order-success { background: #e8f5e9; border: 2px solid #4CAF50; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
        .customer-info { font-size: 13px; color: #666; margin-bottom: 10px; }
    </style>
</head>
<body>
    <div class="navbar">
        <h2>SYOS - Online Shop</h2>
        <div>
            <%
                OnlineCustomer customer = (OnlineCustomer) session.getAttribute("customer");
            %>
            <span>Hello, <%= customer.getName() %></span>
            <a href="orders">My Orders</a>
            <a href="dashboard">Dashboard</a>
            <a href="logout">Logout</a>
        </div>
    </div>

    <div style="max-width:1400px;margin:10px auto;padding:0 20px;">
        <%
            String shopMessage = (String) session.getAttribute("shopMessage");
            String shopError = (String) session.getAttribute("shopError");
            if (shopMessage != null) { session.removeAttribute("shopMessage"); %>
                <div class="success"><%= shopMessage %></div>
        <% } %>
        <% if (shopError != null) { session.removeAttribute("shopError"); %>
                <div class="error"><%= shopError %></div>
        <% } %>

        <%-- Show completed order if one was just placed --%>
        <%
            Bill completedOrder = (Bill) session.getAttribute("completedOrder");
            if (completedOrder != null) {
                session.removeAttribute("completedOrder");
        %>
        <div class="order-success">
            <h3>Order #<%= completedOrder.getSerialNumber() %> Confirmed!</h3>
            <p>Delivery to: <%= customer.getDeliveryAddress() %></p>
            <table>
                <tr><th>Item</th><th>Qty</th><th>Price</th><th>Total</th></tr>
                <% for (BillItem bi : completedOrder.getItems()) { %>
                <tr>
                    <td><%= bi.getName() %></td>
                    <td><%= bi.getQuantity() %></td>
                    <td>Rs. <%= String.format("%.2f", bi.getUnitPrice()) %></td>
                    <td>Rs. <%= String.format("%.2f", bi.getTotalPrice()) %></td>
                </tr>
                <% } %>
            </table>
            <p><strong>Total: Rs. <%= String.format("%.2f", completedOrder.getFinalAmount()) %></strong></p>
        </div>
        <% } %>
    </div>

    <div class="container">
        <%-- LEFT: Product Grid --%>
        <div class="panel">
            <h3>Products</h3>
            <div class="items-grid">
                <%
                    List<Item> allItems = (List<Item>) request.getAttribute("allItems");
                    InventoryService invService = (InventoryService) request.getAttribute("inventoryService");
                    if (allItems != null) {
                        for (Item item : allItems) {
                            int stock = invService.getTotalQuantity(item.getItemCode(), StoreType.ONLINE_STORE);
                %>
                <div class="item-card">
                    <h4><%= item.getName() %></h4>
                    <div class="price">Rs. <%= String.format("%.2f", item.getUnitPrice()) %></div>
                    <% if (stock > 0) { %>
                        <div class="stock <%= stock < 10 ? "low" : "" %>">
                            <%= stock %> in stock
                        </div>
                        <form method="post" action="shop">
                            <input type="hidden" name="action" value="addToCart">
                            <input type="hidden" name="itemCode" value="<%= item.getItemCode() %>">
                            <input type="number" name="quantity" value="1" min="1" max="<%= stock %>">
                            <button type="submit">Add</button>
                        </form>
                    <% } else { %>
                        <div class="out-of-stock">Out of Stock</div>
                    <% } %>
                </div>
                <%      }
                    }
                %>
            </div>
        </div>

        <%-- RIGHT: Shopping Cart --%>
        <div class="panel">
            <h3>Shopping Cart</h3>
            <div class="customer-info">
                Delivering to: <%= customer.getDeliveryAddress() %>
            </div>
            <%
                PointOfSaleService.ShoppingCart cart =
                    (PointOfSaleService.ShoppingCart) request.getAttribute("cart");
                if (cart != null && cart.getItemCount() > 0) {
            %>
                <table>
                    <tr><th>Item</th><th>Qty</th><th>Total</th></tr>
                    <% for (BillItem bi : cart.getBill().getItems()) { %>
                    <tr>
                        <td><%= bi.getName() %></td>
                        <td><%= bi.getQuantity() %></td>
                        <td>Rs. <%= String.format("%.2f", bi.getTotalPrice()) %></td>
                    </tr>
                    <% } %>
                </table>

                <div class="cart-total">
                    Total: Rs. <%= String.format("%.2f", cart.getTotal()) %>
                </div>

                <form method="post" action="shop">
                    <input type="hidden" name="action" value="checkout">
                    <button type="submit" class="btn btn-checkout">Place Order</button>
                </form>
                <form method="post" action="shop">
                    <input type="hidden" name="action" value="clearCart">
                    <button type="submit" class="btn btn-clear">Clear Cart</button>
                </form>
            <% } else { %>
                <p style="color:#999; text-align:center; padding:30px 0;">Your cart is empty</p>
            <% } %>
        </div>
    </div>
</body>
<script>
    /**
     * Real-time stock polling for the online shop.
     * 
     * Same concept as the POS polling, but updates the product cards.
     * When an online customer buys something, other customers see
     * the stock count change in real time.
     * 
     * Also disables the "Add" button if stock drops to zero,
     * preventing customers from trying to buy out-of-stock items.
     */
    function pollStockUpdates() {
        fetch('api/stock?store=online')
            .then(function(response) { return response.json(); })
            .then(function(items) {
                var cards = document.querySelectorAll('.item-card');

                cards.forEach(function(card) {
                    // Find the item code from the hidden input in the form
                    var hiddenInput = card.querySelector('input[name="itemCode"]');
                    if (!hiddenInput) return;
                    var code = hiddenInput.value;

                    for (var i = 0; i < items.length; i++) {
                        if (items[i].code === code) {
                            var stockDiv = card.querySelector('.stock');
                            var outOfStock = card.querySelector('.out-of-stock');

                            if (items[i].stock > 0) {
                                // Item is in stock
                                if (stockDiv) {
                                    var oldText = stockDiv.textContent.trim();
                                    var newText = items[i].stock + ' in stock';
                                    if (oldText !== newText) {
                                        stockDiv.textContent = newText;
                                        stockDiv.style.backgroundColor = '#fff9c4';
                                        setTimeout(function() {
                                            stockDiv.style.backgroundColor = '';
                                        }, 1000);
                                    }
                                    // Update max on quantity input
                                    var qtyInput = card.querySelector('input[name="quantity"]');
                                    if (qtyInput) qtyInput.max = items[i].stock;
                                }
                            } else {
                                // Item went out of stock — need to update the card
                                if (stockDiv) {
                                    stockDiv.className = 'out-of-stock';
                                    stockDiv.textContent = 'Out of Stock';
                                    // Remove the add form
                                    var form = card.querySelector('form');
                                    if (form) form.style.display = 'none';
                                }
                            }
                            break;
                        }
                    }
                });
            })
            .catch(function(error) {
                console.log('Stock poll error:', error);
            });
    }

    setInterval(pollStockUpdates, 3000);
</script>
</html>