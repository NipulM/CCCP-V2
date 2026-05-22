package com.cb011999.cccp.web;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.model.Item;
import com.cb011999.cccp.repository.ItemRepository;
import com.cb011999.cccp.service.InventoryService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * API endpoint that returns current stock levels as JSON.
 * 
 * The POS and Shop pages use JavaScript to call this endpoint
 * every few seconds (polling). If stock has changed (because another
 * user made a purchase), the page updates automatically without
 * a full reload.
 */
@WebServlet("/api/stock")
public class StockUpdateServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Set response type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ItemRepository itemRepo = (ItemRepository) getServletContext().getAttribute("itemRepo");
        InventoryService inventoryService = (InventoryService) getServletContext().getAttribute("inventoryService");

        // Determine which store type to return stock for
        String store = request.getParameter("store");
        StoreType storeType = "online".equals(store)
                ? StoreType.ONLINE_STORE
                : StoreType.PHYSICAL_STORE;

        List<Item> allItems = itemRepo.findAll();

        // Build JSON manually — no external library needed
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < allItems.size(); i++) {
            Item item = allItems.get(i);
            int stock = inventoryService.getTotalQuantity(item.getItemCode(), storeType);

            json.append("{");
            json.append("\"code\":\"").append(item.getItemCode()).append("\",");
            json.append("\"name\":\"").append(item.getName()).append("\",");
            json.append("\"price\":").append(item.getUnitPrice()).append(",");
            json.append("\"stock\":").append(stock);
            json.append("}");

            if (i < allItems.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        PrintWriter out = response.getWriter();
        out.print(json.toString());
        out.flush();
    }
}