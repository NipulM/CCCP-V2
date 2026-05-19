package com.cb011999.cccp.web;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.model.StockBatch;
import com.cb011999.cccp.repository.InventoryRepository;
import com.cb011999.cccp.service.InventoryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/inventory")
public class InventoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

//        HttpSession session = request.getSession(false);
//        if (session == null || !"employee".equals(session.getAttribute("userType"))) {
//            response.sendRedirect("login");
//            return;
//        }

        InventoryRepository inventoryRepo = (InventoryRepository) getServletContext().getAttribute("inventoryRepo");

        // Get the view type from the request, default to "shelf"
        String viewType = request.getParameter("view");
        if (viewType == null) viewType = "shelf";

        List<StockBatch> batches;
        String title;

        switch (viewType) {
            case "online":
                batches = inventoryRepo.getAllOnlineStock();
                title = "Online Inventory";
                break;
            case "warehouse":
                batches = inventoryRepo.getAllStoreStock();
                title = "Warehouse Inventory";
                break;
            default:
                batches = inventoryRepo.getAllShelfStock();
                title = "Shelf Inventory (Physical Store)";
                break;
        }

        request.setAttribute("batches", batches);
        request.setAttribute("title", title);
        request.setAttribute("viewType", viewType);

        request.getRequestDispatcher("/WEB-INF/views/inventory.jsp").forward(request, response);
    }
}