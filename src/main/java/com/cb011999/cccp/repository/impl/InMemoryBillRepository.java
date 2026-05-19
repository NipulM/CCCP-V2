package com.cb011999.cccp.repository.impl;

import com.cb011999.cccp.domain.model.Bill;
import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import com.cb011999.cccp.repository.BillRepository;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryBillRepository implements BillRepository {
    private static InMemoryBillRepository instance;
    private final Map<Integer, Bill> bills;
    
    private InMemoryBillRepository() {
        this.bills = new HashMap<>();
    }
    
    public static synchronized InMemoryBillRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryBillRepository();
        }
        return instance;
    }
    
    @Override
    public void save(Bill bill) {
        bills.put(bill.getSerialNumber(), bill);
    }
    
    @Override
    public Optional<Bill> findBySerialNumber(int serialNumber) {
        return Optional.ofNullable(bills.get(serialNumber));
    }
    
    @Override
    public List<Bill> findByDate(LocalDate date) {
        return bills.values().stream()
            .filter(bill -> bill.getBillDate().toLocalDate().equals(date))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Bill> findByTransactionType(TransactionType transactionType) {
        return bills.values().stream()
            .filter(bill -> bill.getTransactionType() == transactionType)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Bill> findByStoreType(StoreType storeType) {
        return bills.values().stream()
            .filter(bill -> bill.getStoreType() == storeType)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Bill> findByFilters(LocalDate date, TransactionType transactionType, 
                                     StoreType storeType) {
        return bills.values().stream()
            .filter(bill -> date == null || 
                          bill.getBillDate().toLocalDate().equals(date))
            .filter(bill -> transactionType == null || 
                          bill.getTransactionType() == transactionType)
            .filter(bill -> storeType == null || 
                          bill.getStoreType() == storeType)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Bill> findAll() {
        return new ArrayList<>(bills.values());
    }
    
    public void clear() {
        bills.clear();
    }
}