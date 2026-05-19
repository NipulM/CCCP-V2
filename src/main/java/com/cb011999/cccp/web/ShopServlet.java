package com.cb011999.cccp.web;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.model.Item;
import com.cb011999.cccp.domain.model.OnlineCustomer;
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

@WebServlet("/shop")
public class ShopServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"customer".equals(session.getAttribute("userType"))) {
            response.sendRedirect("login");
            return;
        }

        ItemRepository itemRepo = (ItemRepository) getServletContext().getAttribute("itemRepo");
        InventoryService inventoryService = (InventoryService) getServletContext().getAttribute("inventoryService");

        List<Item> allItems = itemRepo.findAll();
        request.setAttribute("allItems", allItems);
        request.setAttribute("inventoryService", inventoryService);

        // Get existing cart from session if there is one
        PointOfSaleService.ShoppingCart cart =
                (PointOfSaleService.ShoppingCart) session.getAttribute("cart");
        request.setAttribute("cart", cart);

        request.getRequestDispatcher("/WEB-INF/views/shop.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || !"customer".equals(session.getAttribute("userType"))) {
            response.sendRedirect("login");
            return;
        }

        String action = request.getParameter("action");
        PointOfSaleService posService = (PointOfSaleService) getServletContext().getAttribute("posService");

        switch (action) {
            case "addToCart":
                handleAddToCart(request, session, posService);
                break;
            case "checkout":
                handleCheckout(request, session, posService);
                break;
            case "clearCart":
                session.removeAttribute("cart");
                break;
        }

        response.sendRedirect("shop");
    }

    private void handleAddToCart(HttpServletRequest request, HttpSession session,
                                PointOfSaleService posService) {

        // Get or create the shopping cart
        PointOfSaleService.ShoppingCart cart =
                (PointOfSaleService.ShoppingCart) session.getAttribute("cart");

        if (cart == null) {
            OnlineCustomer customer = (OnlineCustomer) session.getAttribute("customer");
            cart = posService.createOnlineShoppingCart(customer);
            session.setAttribute("cart", cart);
        }

        String itemCode = request.getParameter("itemCode");
        int quantity;
        try {
            quantity = Integer.parseInt(request.getParameter("quantity"));
        } catch (NumberFormatException e) {
            session.setAttribute("shopError", "Invalid quantity");
            return;
        }

        PointOfSaleService.AddItemResult result = posService.addItemToBill(cart.getBill(), itemCode, quantity);

        if (result.isSuccess()) {
            session.setAttribute("shopMessage", "Item added to cart");
        } else {
            session.setAttribute("shopError", result.getMessage());
        }
    }

    private void handleCheckout(HttpServletRequest request, HttpSession session,
                                PointOfSaleService posService) {

        PointOfSaleService.ShoppingCart cart =
                (PointOfSaleService.ShoppingCart) session.getAttribute("cart");

        if (cart == null || cart.getItemCount() == 0) {
            session.setAttribute("shopError", "Your cart is empty");
            return;
        }

        // Online payments are simulated — the customer pays the exact total
        // In a real system this would integrate with a payment gateway
        CashPayment payment = new CashPayment(cart.getTotal(), cart.getTotal());

        PointOfSaleService.CheckoutResult result = posService.checkoutOnlineOrder(cart, payment);

        if (result.isSuccess()) {
            session.setAttribute("completedOrder", result.getBill());
            session.removeAttribute("cart");
            session.setAttribute("shopMessage",
                    "Order placed successfully! Order #" + result.getBill().getSerialNumber());
        } else {
            session.setAttribute("shopError", result.getMessage());
        }
    }
}