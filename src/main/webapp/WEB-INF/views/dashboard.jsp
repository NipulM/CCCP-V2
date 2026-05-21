<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>SYOS - Dashboard</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f0f2f5; }
        .navbar { background: #333; color: white; padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; }
        .navbar h2 { margin: 0; }
        .navbar a { color: white; text-decoration: none; margin-left: 20px; }
        .container { max-width: 1200px; margin: 30px auto; padding: 0 20px; }
        .welcome { font-size: 24px; margin-bottom: 30px; color: #333; }
        .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; }
        .card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; cursor: pointer; transition: transform 0.2s; }
        .card:hover { transform: translateY(-5px); }
        .card h3 { color: #333; margin-bottom: 10px; }
        .card p { color: #666; }
        .card a { text-decoration: none; color: inherit; display: block; }
    </style>
</head>
<body>
    <div class="navbar">
        <h2>SYOS - Synex Outlet Store</h2>
        <div>
            <span>Welcome, <%= session.getAttribute("userName") %></span>
            <a href="logout">Logout</a>
        </div>
    </div>

    <div class="container">
        <div class="welcome">Dashboard</div>

        <div class="grid">
            <% if ("employee".equals(session.getAttribute("userType"))) { %>
                <div class="card">
                    <a href="pos">
                        <h3>Point of Sale</h3>
                        <p>Process over-the-counter sales</p>
                    </a>
                </div>
                <div class="card">
                    <a href="inventory">
                        <h3>Inventory</h3>
                        <p>View and manage stock</p>
                    </a>
                </div>
                <div class="card">
                    <a href="reports">
                        <h3>Reports</h3>
                        <p>View sales and stock reports</p>
                    </a>
                </div>
                <div class="card">
                    <a href="stock">
                        <h3>Stock Management</h3>
                        <p>Add stock and restock shelves</p>
                    </a>
                </div>
            <% } else { %>
                <div class="card">
                    <a href="shop">
                        <h3>Shop Online</h3>
                        <p>Browse and purchase items</p>
                    </a>
                </div>
                <div class="card">
                    <a href="orders">
                        <h3>My Orders</h3>
                        <p>View your order history</p>
                    </a>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html>