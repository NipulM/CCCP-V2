package com.cb011999.cccp.web;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.model.Item;
import com.cb011999.cccp.repository.ItemRepository;
import com.cb011999.cccp.service.InventoryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/stock")
public class StockServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

//        HttpSession session = request.getSession(false);
//        if (session == null || !"employee".equals(session.getAttribute("userType"))) {
//            response.sendRedirect("login");
//            return;
//        }

        ItemRepository itemRepo = (ItemRepository) getServletContext().getAttribute("itemRepo");
        List<Item> allItems = itemRepo.findAll();
        request.setAttribute("allItems", allItems);

        request.getRequestDispatcher("/WEB-INF/views/stock.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"employee".equals(session.getAttribute("userType"))) {
            response.sendRedirect("login");
            return;
        }

        String action = request.getParameter("action");
        InventoryService inventoryService = (InventoryService) getServletContext().getAttribute("inventoryService");
        ItemRepository itemRepo = (ItemRepository) getServletContext().getAttribute("itemRepo");

        switch (action) {
            case "addStock":
                handleAddStock(request, session, inventoryService, itemRepo);
                break;
            case "restock":
                handleRestock(request, session, inventoryService, itemRepo);
                break;
        }

        response.sendRedirect("stock");
    }

    private void handleAddStock(HttpServletRequest request, HttpSession session,
                                InventoryService inventoryService, ItemRepository itemRepo) {
        String itemCode = request.getParameter("itemCode");

        if (!itemRepo.exists(itemCode)) {
            session.setAttribute("stockError", "Item not found: " + itemCode);
            return;
        }

        try {
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            int daysUntilExpiry = Integer.parseInt(request.getParameter("daysUntilExpiry"));

            LocalDate purchaseDate = LocalDate.now();
            LocalDate expiryDate = LocalDate.now().plusDays(daysUntilExpiry);

            inventoryService.addStockToWarehouse(itemCode, quantity, purchaseDate, expiryDate);
            session.setAttribute("stockMessage", "Added " + quantity + " units of " + itemCode + " to warehouse");
        } catch (NumberFormatException e) {
            session.setAttribute("stockError", "Invalid number entered");
        }
    }

    private void handleRestock(HttpServletRequest request, HttpSession session,
                               InventoryService inventoryService, ItemRepository itemRepo) {
        String itemCode = request.getParameter("itemCode");

        if (!itemRepo.exists(itemCode)) {
            session.setAttribute("stockError", "Item not found: " + itemCode);
            return;
        }

        try {
            int quantity = Integer.parseInt(request.getParameter("quantity"));
            String target = request.getParameter("target");

            StoreType storeType = "online".equals(target) ? StoreType.ONLINE_STORE : StoreType.PHYSICAL_STORE;

            int moved = inventoryService.restockShelves(itemCode, quantity, storeType);

            if (moved > 0) {
                session.setAttribute("stockMessage", "Moved " + moved + " units of " + itemCode + " to " + target);
            } else {
                session.setAttribute("stockError", "Could not restock. Check warehouse availability.");
            }
        } catch (NumberFormatException e) {
            session.setAttribute("stockError", "Invalid number entered");
        }
    }
}