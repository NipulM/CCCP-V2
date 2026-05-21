package com.cb011999.cccp.web;

import com.cb011999.cccp.service.UserService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * API endpoint for creating employees.
 * Protected by a static API key — call from Postman with the key to create employees.
 *
 * Usage (Postman):
 * POST /api/employees
 * Content-Type: application/x-www-form-urlencoded
 *
 * Body:
 *   apiKey=SYOS-ADMIN-2026
 *   name=John Silva
 *   contact=0771234567
 *   employeeNumber=E001
 *   role=Cashier
 *   password=password123
 */
@WebServlet("/api/employees")
public class AdminServlet extends HttpServlet {

    private static final String API_KEY = "SYOS-ADMIN-2026";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Verify API key
        String apiKey = request.getParameter("apiKey");
        if (!API_KEY.equals(apiKey)) {
            response.setStatus(401);
            out.print("{\"success\":false,\"message\":\"Invalid API key\"}");
            out.flush();
            return;
        }

        String name = request.getParameter("name");
        String contact = request.getParameter("contact");
        String employeeNumber = request.getParameter("employeeNumber");
        String role = request.getParameter("role");
        String password = request.getParameter("password");

        UserService userService = (UserService) getServletContext().getAttribute("userService");

        UserService.RegistrationResult result = userService.registerEmployee(
                name, contact, employeeNumber, role, password);

        if (result.isSuccess()) {
            response.setStatus(201);
            out.print("{\"success\":true,\"message\":\"" + result.getMessage()
                    + "\",\"employeeNumber\":\"" + employeeNumber + "\"}");
        } else {
            response.setStatus(400);
            out.print("{\"success\":false,\"message\":\"" + result.getMessage() + "\"}");
        }
        out.flush();
    }
}