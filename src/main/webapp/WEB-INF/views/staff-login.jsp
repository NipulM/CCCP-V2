<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>SYOS - Staff Login</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f0f2f5; display: flex; justify-content: center; align-items: center; min-height: 100vh; }
        .login-container { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); width: 400px; }
        h1 { text-align: center; color: #333; margin-bottom: 5px; }
        h3 { text-align: center; color: #666; margin-bottom: 30px; font-weight: normal; }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 5px; color: #555; font-weight: bold; }
        input[type="text"], input[type="password"] { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
        button { width: 100%; padding: 12px; background: #333; color: white; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; }
        button:hover { background: #555; }
        .error { background: #ffebee; color: #c62828; padding: 10px; border-radius: 4px; margin-bottom: 20px; text-align: center; }
        .back-link { text-align: center; margin-top: 15px; }
        .back-link a { color: #666; text-decoration: none; }
        .staff-badge { text-align: center; margin-bottom: 20px; }
        .staff-badge span { background: #333; color: white; padding: 4px 12px; border-radius: 12px; font-size: 12px; }
    </style>
</head>
<body>
    <div class="login-container">
        <h1>Synex Outlet Store</h1>
        <div class="staff-badge"><span>STAFF PORTAL</span></div>
        <h3>Employee Login</h3>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form method="post" action="staff">
            <div class="form-group">
                <label>Employee Number:</label>
                <input type="text" name="employeeNumber" placeholder="e.g. E001" required>
            </div>
            <div class="form-group">
                <label>Password:</label>
                <input type="password" name="password" placeholder="Enter password" required>
            </div>
            <button type="submit">Login</button>
        </form>

        <div class="back-link">
            <a href="./">← Back to store</a>
        </div>
    </div>
</body>
</html>