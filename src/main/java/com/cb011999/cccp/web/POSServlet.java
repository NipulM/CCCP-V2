package com.cb011999.cccp.web;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.domain.model.Bill;
import com.cb011999.cccp.domain.model.Item;
import com.cb011999.cccp.repository.ItemRepository;
import com.cb011999.cccp.service.InventoryService;
import com.cb011999.cccp.service.PointOfSaleService;
import com.cb011999.cccp.strategy.CashPayment;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/pos")
public class POSServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"employee".equals(session.getAttribute("userType"))) {
            response.sendRedirect("login");
            return;
        }

        // Load all items so the employee can see what's available
        ItemRepository itemRepo = (ItemRepository) getServletContext().getAttribute("itemRepo");
        InventoryService inventoryService = (InventoryService) getServletContext().getAttribute("inventoryService");

        List<Item> allItems = itemRepo.findAll();
        request.setAttribute("allItems", allItems);
        request.setAttribute("inventoryService", inventoryService);

        // Get the current bill from session (if one exists)
        Bill currentBill = (Bill) session.getAttribute("currentBill");
        request.setAttribute("currentBill", currentBill);

        request.getRequestDispatcher("/WEB-INF/views/pos.jsp").forward(request, response);
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
        PointOfSaleService posService = (PointOfSaleService) getServletContext().getAttribute("posService");

        switch (action) {
            case "newBill":
                handleNewBill(request, session, posService);
                break;
            case "addItem":
                handleAddItem(request, session, posService);
                break;
            case "checkout":
                handleCheckout(request, session, posService);
                break;
            case "cancelBill":
                session.removeAttribute("currentBill");
                break;
        }

        // Redirect back to GET to avoid form resubmission
        // This is called the POST-Redirect-GET pattern — it prevents
        // the browser from re-posting the form if the user refreshes the page
        response.sendRedirect("pos");
    }

    private void handleNewBill(HttpServletRequest request, HttpSession session,
                               PointOfSaleService posService) {
        // Create a fresh bill for an over-the-counter sale
        Bill bill = posService.createBill(TransactionType.OVER_THE_COUNTER, StoreType.PHYSICAL_STORE);
        session.setAttribute("currentBill", bill);
    }

    private void handleAddItem(HttpServletRequest request, HttpSession session,
                               PointOfSaleService posService) {
        Bill bill = (Bill) session.getAttribute("currentBill");
        if (bill == null) return;

        String itemCode = request.getParameter("itemCode");
        int quantity;
        try {
            quantity = Integer.parseInt(request.getParameter("quantity"));
        } catch (NumberFormatException e) {
            session.setAttribute("posError", "Invalid quantity");
            return;
        }

        PointOfSaleService.AddItemResult result = posService.addItemToBill(bill, itemCode, quantity);

        if (result.isSuccess()) {
            session.setAttribute("posMessage", result.getMessage());
        } else {
            session.setAttribute("posError", result.getMessage());
        }
    }

    private void handleCheckout(HttpServletRequest request, HttpSession session,
                                PointOfSaleService posService) {
        Bill bill = (Bill) session.getAttribute("currentBill");
        if (bill == null) return;

        // Apply discount if provided
        String discountStr = request.getParameter("discount");
        if (discountStr != null && !discountStr.trim().isEmpty()) {
            try {
                double discount = Double.parseDouble(discountStr);
                if (discount > 0) {
                    posService.applyDiscount(bill, discount);
                }
            } catch (NumberFormatException e) {
                // Ignore invalid discount
            }
        }

        // Process cash payment
        double cashTendered;
        try {
            cashTendered = Double.parseDouble(request.getParameter("cashTendered"));
        } catch (NumberFormatException e) {
            session.setAttribute("posError", "Invalid cash amount");
            return;
        }

        CashPayment payment = new CashPayment(cashTendered, bill.getFinalAmount());
        PointOfSaleService.CheckoutResult result = posService.checkout(bill, payment, cashTendered);

        if (result.isSuccess()) {
            // Store the completed bill for display, then clear the active bill
            session.setAttribute("completedBill", result.getBill());
            session.removeAttribute("currentBill");
            session.setAttribute("posMessage", "Checkout successful! Change: Rs. " +
                    String.format("%.2f", result.getBill().getChange()));
        } else {
            session.setAttribute("posError", result.getMessage());
        }
    }
}