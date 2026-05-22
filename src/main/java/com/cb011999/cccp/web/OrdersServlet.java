package com.cb011999.cccp.web;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.service.ReportService;
import com.cb011999.cccp.service.report.model.BillReport;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/orders")
public class OrdersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"customer".equals(session.getAttribute("userType"))) {
            response.sendRedirect("login");
            return;
        }

        ReportService reportService = (ReportService) getServletContext().getAttribute("reportService");

        // Get all online transactions — shows all online orders
        // In a full system you'd filter by customer ID
        BillReport billReport = reportService.generateBillReport(
                TransactionType.ONLINE, StoreType.ONLINE_STORE);

        request.setAttribute("billReport", billReport);

        request.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(request, response);
    }
}