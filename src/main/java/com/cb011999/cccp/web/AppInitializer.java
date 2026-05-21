package com.cb011999.cccp.web;

import com.cb011999.cccp.database.DatabaseConnection;
import com.cb011999.cccp.factory.RepositoryFactory;
import com.cb011999.cccp.observer.ConsoleStockObserver;
import com.cb011999.cccp.observer.EmailStockObserver;
import com.cb011999.cccp.repository.*;
import com.cb011999.cccp.service.*;
import com.cb011999.cccp.service.concurrency.SynchronizedInventoryService;
import com.cb011999.cccp.service.concurrency.SynchronizedPointOfSaleService;
import com.cb011999.cccp.service.concurrency.SynchronizedReportService;
import com.cb011999.cccp.service.concurrency.SynchronizedUserService;
import com.cb011999.cccp.service.impl.InventoryServiceImpl;
import com.cb011999.cccp.service.impl.PointOfSaleServiceImpl;
import com.cb011999.cccp.service.impl.ReportServiceImpl;
import com.cb011999.cccp.service.impl.UserServiceImpl;
import com.cb011999.cccp.web.concurrency.RequestQueue;

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
        
        // Initialize request queue
        RequestQueue.getInstance();

        // Create base implementations
        InventoryServiceImpl inventoryImpl = new InventoryServiceImpl(inventoryRepo);
        PointOfSaleServiceImpl posImpl = new PointOfSaleServiceImpl(itemRepo, billRepo, inventoryImpl);
        ReportServiceImpl reportImpl = new ReportServiceImpl(billRepo, itemRepo, inventoryImpl);
        UserServiceImpl userImpl = new UserServiceImpl(userRepo);
        
        InventoryService inventoryService = new SynchronizedInventoryService(inventoryImpl);
        PointOfSaleService posService = new SynchronizedPointOfSaleService(posImpl);
        ReportService reportService = new SynchronizedReportService(reportImpl);
        UserService userService = new SynchronizedUserService(userImpl);

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