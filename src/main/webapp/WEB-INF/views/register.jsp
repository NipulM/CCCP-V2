<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>SYOS - Register</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f0f2f5; display: flex; justify-content: center; align-items: center; min-height: 100vh; }
        .register-container { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); width: 400px; }
        h1 { text-align: center; color: #333; margin-bottom: 10px; }
        h3 { text-align: center; color: #666; margin-bottom: 30px; font-weight: normal; }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 5px; color: #555; font-weight: bold; }
        input[type="text"], input[type="email"], input[type="password"], input[type="tel"], textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
        textarea { resize: vertical; height: 80px; font-family: Arial, sans-serif; }
        button { width: 100%; padding: 12px; background: #4CAF50; color: white; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; }
        button:hover { background: #45a049; }
        .error { background: #ffebee; color: #c62828; padding: 10px; border-radius: 4px; margin-bottom: 20px; text-align: center; }
        .login-link { text-align: center; margin-top: 15px; }
        .login-link a { color: #4CAF50; text-decoration: none; }
        .hint { font-size: 12px; color: #999; margin-top: 4px; }
    </style>
</head>
<body>
    <div class="register-container">
        <h1>SYOS</h1>
        <h3>Create an Account</h3>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form method="post" action="register">
            <div class="form-group">
                <label>Full Name:</label>
                <input type="text" name="name" placeholder="Enter your full name" value="<%= request.getParameter("name") != null ? request.getParameter("name") : "" %>">
            </div>

            <div class="form-group">
                <label>Contact Number:</label>
                <input type="tel" name="contact" placeholder="Enter your contact number" value="<%= request.getParameter("contact") != null ? request.getParameter("contact") : "" %>">
            </div>

            <div class="form-group">
                <label>Email:</label>
                <input type="email" name="email" placeholder="Enter your email" value="<%= request.getParameter("email") != null ? request.getParameter("email") : "" %>">
            </div>

            <div class="form-group">
                <label>Password:</label>
                <input type="password" name="password" placeholder="Enter your password">
                <p class="hint">Must be at least 8 characters.</p>
            </div>

            <div class="form-group">
                <label>Delivery Address:</label>
                <textarea name="address" placeholder="Enter your delivery address"><%= request.getParameter("address") != null ? request.getParameter("address") : "" %></textarea>
            </div>

            <button type="submit">Register</button>
        </form>

        <div class="login-link">
            <a href="login">Already have an account? Login here</a>
        </div>
    </div>
</body>
</html>