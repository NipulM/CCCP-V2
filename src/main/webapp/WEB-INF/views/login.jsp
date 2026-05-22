<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>SYOS - Customer Login</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f0f2f5; display: flex; justify-content: center; align-items: center; min-height: 100vh; }
        .login-container { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); width: 400px; }
        h1 { text-align: center; color: #2e7d32; margin-bottom: 5px; }
        h3 { text-align: center; color: #666; margin-bottom: 30px; font-weight: normal; }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 5px; color: #555; font-weight: bold; }
        input[type="email"], input[type="password"] { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
        button { width: 100%; padding: 12px; background: #2e7d32; color: white; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; }
        button:hover { background: #1b5e20; }
        .error { background: #ffebee; color: #c62828; padding: 10px; border-radius: 4px; margin-bottom: 20px; text-align: center; }
        .register-link { text-align: center; margin-top: 15px; }
        .register-link a { color: #2e7d32; text-decoration: none; }
        .back-link { text-align: center; margin-top: 10px; }
        .back-link a { color: #666; text-decoration: none; font-size: 14px; }
    </style>
</head>
<body>
    <div class="login-container">
        <h1>Synex Outlet Store</h1>
        <h3>Customer Login</h3>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form method="post" action="login">
            <div class="form-group">
                <label>Email:</label>
                <input type="email" name="email" placeholder="Enter your email" required>
            </div>
            <div class="form-group">
                <label>Password:</label>
                <input type="password" name="password" placeholder="Enter your password" required>
            </div>
            <button type="submit">Login</button>
        </form>

        <div class="register-link">
            <a href="register">New customer? Register here</a>
        </div>
        <div class="back-link">
            <a href="./">← Back to store</a>
        </div>
    </div>
</body>
</html>