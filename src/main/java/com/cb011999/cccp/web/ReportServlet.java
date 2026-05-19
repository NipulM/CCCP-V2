package com.cb011999.cccp.web;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.service.ReportService;
import com.cb011999.cccp.service.report.model.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/reports")
public class ReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"employee".equals(session.getAttribute("userType"))) {
            response.sendRedirect("login");
            return;
        }

        ReportService reportService = (ReportService) getServletContext().getAttribute("reportService");

        String reportType = request.getParameter("type");
        if (reportType == null) reportType = "daily";

        // Parse optional filters from the request
        // These let the employee filter reports by transaction type and store type
        // which fulfils the "combinedly and severally" requirement from the brief
        TransactionType transactionType = null;
        String txFilter = request.getParameter("transactionType");
        if (txFilter != null && !txFilter.isEmpty()) {
            try {
                transactionType = TransactionType.valueOf(txFilter);
            } catch (IllegalArgumentException e) {
                // ignore invalid filter
            }
        }

        StoreType storeType = null;
        String storeFilter = request.getParameter("storeType");
        if (storeFilter != null && !storeFilter.isEmpty()) {
            try {
                storeType = StoreType.valueOf(storeFilter);
            } catch (IllegalArgumentException e) {
                // ignore invalid filter
            }
        }

        // Generate the requested report
        switch (reportType) {
            case "daily":
                DailySalesReport dailyReport = reportService.generateDailySalesReport(
                        LocalDate.now(), transactionType, storeType);
                request.setAttribute("dailyReport", dailyReport);
                break;
            case "reshelf":
                // Default to physical store if no filter, since reshelf is mainly for shelves
                StoreType reshelfStore = storeType != null ? storeType : StoreType.PHYSICAL_STORE;
                ReshelfReport reshelfReport = reportService.generateReshelfReport(reshelfStore, 3);
                request.setAttribute("reshelfReport", reshelfReport);
                break;
            case "reorder":
                StoreType reorderStore = storeType != null ? storeType : StoreType.PHYSICAL_STORE;
                ReorderReport reorderReport = reportService.generateReorderReport(reorderStore);
                request.setAttribute("reorderReport", reorderReport);
                break;
            case "stock":
                StockReport stockReport = reportService.generateStockReport();
                request.setAttribute("stockReport", stockReport);
                break;
            case "bill":
                BillReport billReport = reportService.generateBillReport(transactionType, storeType);
                request.setAttribute("billReport", billReport);
                break;
        }

        request.setAttribute("reportType", reportType);
        request.setAttribute("transactionFilter", txFilter);
        request.setAttribute("storeFilter", storeFilter);

        request.getRequestDispatcher("/WEB-INF/views/reports.jsp").forward(request, response);
    }
}