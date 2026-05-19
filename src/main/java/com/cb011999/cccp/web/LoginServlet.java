package com.cb011999.cccp.web;

import com.cb011999.cccp.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String loginType = request.getParameter("loginType");

        UserService userService = (UserService) getServletContext().getAttribute("userService");

        if ("employee".equals(loginType)) {
//             For now, employees go straight to dashboard
            HttpSession session = request.getSession();
            session.setAttribute("userType", "employee");
            session.setAttribute("userName", "Employee");
            response.sendRedirect("dashboard");
            return;
        }

        // Online customer login
        UserService.LoginResult result = userService.loginWithEmail(email, password);

        if (result.isSuccess()) {
            HttpSession session = request.getSession();
            session.setAttribute("userType", "customer");
            session.setAttribute("customer", result.getCustomer());
            session.setAttribute("userName", result.getCustomer().getName());
            response.sendRedirect("dashboard");
        } else {
            request.setAttribute("error", result.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}