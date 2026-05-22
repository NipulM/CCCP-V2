<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.cb011999.cccp.domain.model.*, com.cb011999.cccp.domain.enums.*, com.cb011999.cccp.service.InventoryService, java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <title>Synex Outlet Store</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f0f2f5; }
        .navbar { background: #2e7d32; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .navbar h2 { margin: 0; }
        .navbar-links { display: flex; align-items: center; gap: 15px; }
        .navbar a { color: white; text-decoration: none; padding: 8px 16px; border-radius: 4px; }
        .navbar a:hover { background: rgba(255,255,255,0.2); }
        .btn-login { background: #2e7d32; color: #2e7d32; font-weight: bold; }
        .btn-login:hover { background: #e8f5e9; }
        .btn-staff { font-size: 12px; opacity: 0.8; }
        .hero { background: #2e7d32; color: white; text-align: center; padding: 40px 20px 50px; }
        .hero h1 { font-size: 32px; margin-bottom: 10px; }
        .hero p { font-size: 16px; opacity: 0.9; }
        .container { max-width: 1200px; margin: 30px auto; padding: 0 20px; }
        .section-title { font-size: 22px; color: #333; margin-bottom: 20px; }
        .items-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
        .item-card { background: white; border-radius: 8px; padding: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }
        .item-card h4 { color: #333; margin-bottom: 8px; font-size: 16px; }
        .item-card .category { color: #888; font-size: 13px; margin-bottom: 10px; }
        .item-card .price { color: #2e7d32; font-size: 22px; font-weight: bold; margin-bottom: 8px; }
        .item-card .stock { font-size: 13px; padding: 4px 10px; border-radius: 12px; display: inline-block; }
        .stock.in-stock { background: #e8f5e9; color: #2e7d32; }
        .stock.low-stock { background: #fff3e0; color: #e65100; }
        .stock.out-of-stock { background: #ffebee; color: #c62828; }
        .login-prompt { background: white; border-radius: 8px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; margin-top: 30px; }
        .login-prompt h3 { margin-bottom: 10px; color: #333; }
        .login-prompt p { color: #666; margin-bottom: 15px; }
        .login-prompt a { display: inline-block; padding: 12px 30px; background: #2e7d32; color: white; text-decoration: none; border-radius: 4px; font-size: 16px; }
        footer { background: #333; color: #aaa; text-align: center; padding: 20px; margin-top: 40px; font-size: 13px; }
    </style>
</head>
<body>
    <div class="navbar">
        <h2>SYOS</h2>
        <div class="navbar-links">
            <%
                String userName = null;
                if (session != null) {
                    userName = (String) session.getAttribute("userName");
                }
                if (userName != null) {
            %>
                <span>Hello, <%= userName %></span>
                <a href="dashboard">Dashboard</a>
                <a href="logout">Logout</a>
            <% } else { %>
                <a href="login" class="btn-login">Login / Register</a>
            <% } %>
        </div>
    </div>

    <div class="hero">
        <h1>Welcome to Synex Outlet Store</h1>
        <p>Fresh groceries and household essentials delivered to your door</p>
    </div>

    <div class="container">
        <h2 class="section-title">Our Products</h2>

        <div class="items-grid">
            <%
                List<Item> allItems = (List<Item>) request.getAttribute("allItems");
                InventoryService invService = (InventoryService) request.getAttribute("inventoryService");
                if (allItems != null) {
                    for (Item item : allItems) {
                        int stock = invService.getTotalQuantity(item.getItemCode(), StoreType.ONLINE_STORE);
                        String stockClass = "in-stock";
                        String stockText = stock + " available";
                        if (stock == 0) {
                            stockClass = "out-of-stock";
                            stockText = "Out of stock";
                        } else if (stock < 10) {
                            stockClass = "low-stock";
                            stockText = "Only " + stock + " left";
                        }
                        String categoryName = item.getCategory() != null ? item.getCategory().getName() : "";
            %>
            <div class="item-card">
                <h4><%= item.getName() %></h4>
                <% if (!categoryName.isEmpty()) { %>
                    <div class="category"><%= categoryName %></div>
                <% } %>
                <div class="price">Rs. <%= String.format("%.2f", item.getUnitPrice()) %></div>
                <span class="stock <%= stockClass %>"><%= stockText %></span>
            </div>
            <%      }
                }
            %>
        </div>

        <% if (userName == null) { %>
        <div class="login-prompt">
            <h3>Ready to shop?</h3>
            <p>Login or create an account to start adding items to your cart</p>
            <a href="login">Login / Register</a>
        </div>
        <% } %>
    </div>

    <footer>
        2026 SYOS - Synex Outlet Store, Colombo
    </footer>
</body>
</html>