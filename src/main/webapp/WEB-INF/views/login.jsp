<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>SYOS - Login</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f0f2f5; display: flex; justify-content: center; align-items: center; min-height: 100vh; }
        .login-container { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); width: 400px; }
        h1 { text-align: center; color: #333; margin-bottom: 10px; }
        h3 { text-align: center; color: #666; margin-bottom: 30px; font-weight: normal; }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 5px; color: #555; font-weight: bold; }
        input[type="email"], input[type="password"], select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
        button { width: 100%; padding: 12px; background: #4CAF50; color: white; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; }
        button:hover { background: #45a049; }
        .error { background: #ffebee; color: #c62828; padding: 10px; border-radius: 4px; margin-bottom: 20px; text-align: center; }
        .register-link { text-align: center; margin-top: 15px; }
        .register-link a { color: #4CAF50; text-decoration: none; }
    </style>
</head>
<body>
    <div class="login-container">
        <h1>SYOS</h1>
        <h3>__Synex__ Outlet Store</h3>

        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form method="post" action="login">
            <div class="form-group">
                <label>Login As:</label>
                <select name="loginType" id="loginType" onchange="toggleFields()">
                    <option value="employee">Employee (In-Store)</option>
                    <option value="customer">Online Customer</option>
                </select>
            </div>

            <div id="customerFields" style="display:none;">
                <div class="form-group">
                    <label>Email:</label>
                    <input type="email" name="email" placeholder="Enter your email">
                </div>
                <div class="form-group">
                    <label>Password:</label>
                    <input type="password" name="password" placeholder="Enter your password">
                </div>
            </div>

            <button type="submit">Login</button>
        </form>

        <div class="register-link">
            <a href="register">New customer? Register here</a>
        </div>
    </div>

    <script>
        function toggleFields() {
            var loginType = document.getElementById('loginType').value;
            var customerFields = document.getElementById('customerFields');
            customerFields.style.display = (loginType === 'customer') ? 'block' : 'none';
        }
        toggleFields();
    </script>
</body>
</html>