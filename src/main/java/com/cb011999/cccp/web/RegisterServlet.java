package com.cb011999.cccp.web;

import com.cb011999.cccp.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String contact = request.getParameter("contact");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String address = request.getParameter("address");

        UserService userService = (UserService) getServletContext().getAttribute("userService");

        UserService.RegistrationResult result = userService.registerCustomer(name, contact, email, password, address);

        if (result.isSuccess()) {
            // Auto-login after successful registration
            HttpSession session = request.getSession();
            session.setAttribute("userType", "customer");
            session.setAttribute("customer", result.getCustomer());
            session.setAttribute("userName", result.getCustomer().getName());
            response.sendRedirect("dashboard");
        } else {
            request.setAttribute("error", result.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
}