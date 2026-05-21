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
import java.io.IOException;
import java.util.List;

@WebServlet("")
public class IndexServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ItemRepository itemRepo = (ItemRepository) getServletContext().getAttribute("itemRepo");
        InventoryService inventoryService = (InventoryService) getServletContext().getAttribute("inventoryService");

        List<Item> allItems = itemRepo.findAll();
        request.setAttribute("allItems", allItems);
        request.setAttribute("inventoryService", inventoryService);

        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}