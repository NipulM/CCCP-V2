package com.cb011999.cccp.service.impl;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.domain.model.Bill;
import com.cb011999.cccp.domain.model.BillItem;
import com.cb011999.cccp.domain.model.Item;
import com.cb011999.cccp.domain.model.StockBatch;
import com.cb011999.cccp.repository.BillRepository;
import com.cb011999.cccp.repository.ItemRepository;
import com.cb011999.cccp.service.InventoryService;
import com.cb011999.cccp.service.ReportService;
import com.cb011999.cccp.service.report.model.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportServiceImpl implements ReportService {

    private final BillRepository billRepository;
    private final ItemRepository itemRepository;
    private final InventoryService inventoryService;

    public ReportServiceImpl(BillRepository billRepository,
                             ItemRepository itemRepository,
                             InventoryService inventoryService) {
        this.billRepository = billRepository;
        this.itemRepository = itemRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public DailySalesReport generateDailySalesReport(LocalDate date,
                                                     TransactionType transactionType,
                                                     StoreType storeType) {
        DailySalesReport report = new DailySalesReport(date);

        List<Bill> bills = billRepository.findByFilters(date, transactionType, storeType);

        Map<String, ItemSales> salesMap = new HashMap<>();

        for (Bill bill : bills) {
            for (BillItem billItem : bill.getItems()) {
                String itemCode = billItem.getItemCode();

                ItemSales sales = salesMap.getOrDefault(itemCode,
                    new ItemSales(itemCode, billItem.getName()));

                sales.addSale(billItem.getQuantity(), billItem.getTotalPrice());
                salesMap.put(itemCode, sales);
            }
        }

        for (ItemSales sales : salesMap.values()) {
            report.addItem(sales.itemCode, sales.itemName,
                          sales.totalQuantity, sales.totalRevenue);
        }

        return report;
    }

    @Override
    public ReshelfReport generateReshelfReport(StoreType storeType, int daysThreshold) {
        ReshelfReport report = new ReshelfReport();

        List<StockBatch> itemsToReshelf = inventoryService.getItemsToReshelf(
            storeType, daysThreshold);

        for (StockBatch batch : itemsToReshelf) {
            Item item = itemRepository.findByCode(batch.getItemCode()).orElse(null);
            String itemName = item != null ? item.getName() : "Unknown";

            long daysToExpiry = batch.getDaysUntilExpiry();
            String reason = String.format("Expires in %d days", daysToExpiry);

            report.addItem(batch.getItemCode(), itemName, batch.getQuantity(), reason);
        }

        return report;
    }

    @Override
    public ReorderReport generateReorderReport(StoreType storeType) {
        ReorderReport report = new ReorderReport();

        List<String> itemsNeedingReorder = inventoryService.getItemsNeedingReorder(storeType);

        for (String itemCode : itemsNeedingReorder) {
            Item item = itemRepository.findByCode(itemCode).orElse(null);
            if (item == null) continue;

            int currentQty = inventoryService.getTotalQuantity(itemCode, storeType);
            int suggestedOrder = ReorderReport.getReorderThreshold() * 2 - currentQty;

            report.addItem(itemCode, item.getName(), currentQty, suggestedOrder);
        }

        return report;
    }

    @Override
    public StockReport generateStockReport() {
        StockReport report = new StockReport();

        List<StockBatch> allStock = inventoryService.getAllWarehouseStock();

        for (StockBatch batch : allStock) {
            Item item = itemRepository.findByCode(batch.getItemCode()).orElse(null);
            String itemName = item != null ? item.getName() : "Unknown";

            report.addBatch(
                batch.getItemCode(),
                itemName,
                batch.getQuantity(),
                batch.getPurchaseDate(),
                batch.getExpiryDate()
            );
        }

        return report;
    }

    @Override
    public BillReport generateBillReport(TransactionType transactionType,
                                         StoreType storeType) {
        BillReport report = new BillReport();

        List<Bill> bills = billRepository.findByFilters(null, transactionType, storeType);

        for (Bill bill : bills) {
            report.addBill(
                bill.getSerialNumber(),
                bill.getBillDate(),
                bill.getTotalAmount(),
                bill.getDiscount(),
                bill.getFinalAmount(),
                bill.getTransactionType(),
                bill.getStoreType(),
                bill.getItems().size()
            );
        }

        return report;
    }

    private static class ItemSales {
        final String itemCode;
        final String itemName;
        int totalQuantity;
        double totalRevenue;

        ItemSales(String itemCode, String itemName) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.totalQuantity = 0;
            this.totalRevenue = 0.0;
        }

        void addSale(int quantity, double revenue) {
            this.totalQuantity += quantity;
            this.totalRevenue += revenue;
        }
    }
}