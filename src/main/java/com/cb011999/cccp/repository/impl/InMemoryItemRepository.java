package com.cb011999.cccp.repository.impl;

import com.cb011999.cccp.domain.model.Item;
import com.cb011999.cccp.repository.ItemRepository;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ItemRepository.
 * Uses Singleton pattern to ensure single instance.
 */
public class InMemoryItemRepository implements ItemRepository {
    private static InMemoryItemRepository instance;
    private final Map<String, Item> items;
    
    private InMemoryItemRepository() {
        this.items = new HashMap<>();
    }
    
    public static synchronized InMemoryItemRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryItemRepository();
        }
        return instance;
    }
    
    @Override
    public Optional<Item> findByCode(String itemCode) {
        return Optional.ofNullable(items.get(itemCode));
    }
    
    @Override
    public List<Item> findByCategory(int categoryId) {
        return items.values().stream()
            .filter(item -> item.getCategory() != null && 
                          item.getCategory().getId() == categoryId)
            .collect(Collectors.toList());
    }
    
    @Override
    public void save(Item item) {
        items.put(item.getItemCode(), item);
    }
    
    @Override
    public boolean delete(String itemCode) {
        return items.remove(itemCode) != null;
    }
    
    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }
    
    @Override
    public boolean exists(String itemCode) {
        return items.containsKey(itemCode);
    }
    
    /**
     * Clears all items (useful for testing).
     */
    public void clear() {
        items.clear();
    }
}