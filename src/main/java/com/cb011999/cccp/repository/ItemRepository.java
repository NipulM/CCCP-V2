package com.cb011999.cccp.repository;

import com.cb011999.cccp.domain.model.Item;
import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Optional<Item> findByCode(String itemCode);

    void save(Item item);
    boolean delete(String itemCode);
    boolean exists(String itemCode);
    
    List<Item> findByCategory(int categoryId);
    List<Item> findAll();
}