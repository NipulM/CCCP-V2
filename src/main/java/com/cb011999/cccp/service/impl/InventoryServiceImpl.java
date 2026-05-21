package com.cb011999.cccp.service.impl;

import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.model.StockBatch;
import com.cb011999.cccp.observer.StockSubject;
import com.cb011999.cccp.repository.InventoryRepository;
import com.cb011999.cccp.service.InventoryService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pure business logic implementation of InventoryService.
 * 
 * This class contains NO concurrency handling — it's purely about
 * inventory rules (FIFO, expiry-based sorting, reorder thresholds).
 * Thread safety is handled by ThreadSafeInventoryService which wraps this.
 */
public class InventoryServiceImpl implements InventoryService {
    private static final int REORDER_THRESHOLD = 50;

    private final InventoryRepository inventoryRepository;
    private final StockSubject stockSubject;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
        this.stockSubject = new StockSubject();
    }

    @Override
    public StockSubject getStockSubject() {
        return stockSubject;
    }

    @Override
    public void addStockToWarehouse(String itemCode, int quantity,
                                    LocalDate purchaseDate, LocalDate expiryDate) {
        StockBatch batch = new StockBatch(itemCode, quantity, purchaseDate, expiryDate);
        inventoryRepository.addToStore(batch);
    }

    @Override
    public int restockShelves(String itemCode, int neededQty, StoreType storeType) {
        List<StockBatch> availableBatches = inventoryRepository.getStoreStock(itemCode)
            .stream()
            .filter(StockBatch::hasStock)
            .sorted(new SmartBatchComparator())
            .collect(Collectors.toList());

        int totalMoved = 0;

        for (StockBatch sourceBatch : availableBatches) {
            if (totalMoved >= neededQty) break;

            int toMove = Math.min(neededQty - totalMoved, sourceBatch.getQuantity());

            sourceBatch.reduceStock(toMove);
            inventoryRepository.updateStoreBatch(sourceBatch);

            StockBatch shelfBatch = new StockBatch(
                itemCode, toMove,
                sourceBatch.getPurchaseDate(),
                sourceBatch.getExpiryDate()
            );
            inventoryRepository.addToInventory(shelfBatch, storeType);

            totalMoved += toMove;
        }

        return totalMoved;
    }

    @Override
    public boolean reduceStock(String itemCode, int quantity, StoreType storeType) {
        int available = getTotalQuantity(itemCode, storeType);
        if (available < quantity) return false;

        List<StockBatch> batches = inventoryRepository.getInventory(itemCode, storeType)
            .stream()
            .filter(StockBatch::hasStock)
            .sorted(Comparator.comparing(StockBatch::getExpiryDate))
            .collect(Collectors.toList());

        int remaining = quantity;

        for (StockBatch batch : batches) {
            if (remaining <= 0) break;

            int toReduce = Math.min(remaining, batch.getQuantity());
            batch.reduceStock(toReduce);
            inventoryRepository.updateInventoryBatch(batch, storeType);
            remaining -= toReduce;
        }

        int newQuantity = getTotalQuantity(itemCode, storeType);
        stockSubject.notifyStockChanged(itemCode, newQuantity, storeType);

        return true;
    }

    @Override
    public int getTotalQuantity(String itemCode, StoreType storeType) {
        return inventoryRepository.getTotalQuantity(itemCode, storeType);
    }

    @Override
    public boolean needsReorder(String itemCode, StoreType storeType) {
        return getTotalQuantity(itemCode, storeType) < REORDER_THRESHOLD;
    }

    @Override
    public List<String> getItemsNeedingReorder(StoreType storeType) {
        List<StockBatch> allStock = storeType == StoreType.PHYSICAL_STORE
            ? inventoryRepository.getAllShelfStock()
            : inventoryRepository.getAllOnlineStock();

        return allStock.stream()
            .collect(Collectors.groupingBy(
                StockBatch::getItemCode,
                Collectors.summingInt(StockBatch::getQuantity)
            ))
            .entrySet().stream()
            .filter(entry -> entry.getValue() < REORDER_THRESHOLD)
            .map(entry -> entry.getKey())
            .collect(Collectors.toList());
    }

    @Override
    public List<StockBatch> getItemsToReshelf(StoreType storeType, int daysThreshold) {
        List<StockBatch> inventory = storeType == StoreType.PHYSICAL_STORE
            ? inventoryRepository.getAllShelfStock()
            : inventoryRepository.getAllOnlineStock();

        return inventory.stream()
            .filter(batch -> batch.getDaysUntilExpiry() <= daysThreshold)
            .collect(Collectors.toList());
    }

    @Override
    public List<StockBatch> getStockBatches(String itemCode) {
        return inventoryRepository.getStoreStock(itemCode);
    }

    @Override
    public List<StockBatch> getAllWarehouseStock() {
        return inventoryRepository.getAllStoreStock();
    }

    private static class SmartBatchComparator implements Comparator<StockBatch> {
        @Override
        public int compare(StockBatch b1, StockBatch b2) {
            int expiryCompare = b1.getExpiryDate().compareTo(b2.getExpiryDate());
            if (expiryCompare != 0) return expiryCompare;
            return b1.getPurchaseDate().compareTo(b2.getPurchaseDate());
        }
    }
}