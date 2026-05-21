package com.cb011999.cccp.web;

import com.cb011999.cccp.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/staff")
public class StaffLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/staff-login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String employeeNumber = request.getParameter("employeeNumber");
        String password = request.getParameter("password");

        UserService userService = (UserService) getServletContext().getAttribute("userService");

        UserService.EmployeeLoginResult result = userService.loginEmployee(employeeNumber, password);

        if (result.isSuccess()) {
            HttpSession session = request.getSession();
            session.setAttribute("userType", "employee");
            session.setAttribute("userName", result.getEmployee().getName());
            session.setAttribute("employeeNumber", result.getEmployee().getEmployeeNumber());
            session.setAttribute("employeeRole", result.getEmployee().getRole());
            response.sendRedirect("dashboard");
        } else {
            request.setAttribute("error", result.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/staff-login.jsp").forward(request, response);
        }
    }
}