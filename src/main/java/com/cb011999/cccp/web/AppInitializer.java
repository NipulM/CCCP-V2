package com.cb011999.cccp.web;

import com.cb011999.cccp.database.DatabaseConnection;
import com.cb011999.cccp.factory.RepositoryFactory;
import com.cb011999.cccp.observer.ConsoleStockObserver;
import com.cb011999.cccp.observer.EmailStockObserver;
import com.cb011999.cccp.repository.*;
import com.cb011999.cccp.service.*;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("SYOS Web Application Starting...");

        // Initialize database
        RepositoryFactory.setUseDatabaseRepositories(true);
        DatabaseConnection.getInstance().initializeDatabase();

        // Create repositories
        ItemRepository itemRepo = RepositoryFactory.createItemRepository();
        BillRepository billRepo = RepositoryFactory.createBillRepository();
        InventoryRepository inventoryRepo = RepositoryFactory.createInventoryRepository();
        UserRepository userRepo = RepositoryFactory.createUserRepository();

        // Create services
        InventoryService inventoryService = new InventoryService(inventoryRepo);
        PointOfSaleService posService = new PointOfSaleService(itemRepo, billRepo, inventoryService);
        ReportService reportService = new ReportService(billRepo, itemRepo, inventoryService);
        UserService userService = new UserService(userRepo);

        // Attach observers
        inventoryService.getStockSubject().attach(new ConsoleStockObserver());
        inventoryService.getStockSubject().attach(new EmailStockObserver("manager@syos.com"));

        // Store services in ServletContext so all servlets can access them
        ServletContext ctx = sce.getServletContext();
        ctx.setAttribute("itemRepo", itemRepo);
        ctx.setAttribute("inventoryRepo", inventoryRepo);
        ctx.setAttribute("inventoryService", inventoryService);
        ctx.setAttribute("posService", posService);
        ctx.setAttribute("reportService", reportService);
        ctx.setAttribute("userService", userService);

        System.out.println("SYOS Web Application Started Successfully!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DatabaseConnection.getInstance().closeConnection();
        System.out.println("SYOS Web Application Stopped.");
    }
}