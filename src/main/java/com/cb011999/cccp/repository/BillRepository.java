package com.cb011999.cccp.repository;

import com.cb011999.cccp.domain.model.Bill;
import com.cb011999.cccp.domain.enums.StoreType;
import com.cb011999.cccp.domain.enums.TransactionType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillRepository {
    void save(Bill bill);
    Optional<Bill> findBySerialNumber(int serialNumber);
    
    List<Bill> findByDate(LocalDate date);
    List<Bill> findByTransactionType(TransactionType transactionType);
    List<Bill> findByStoreType(StoreType storeType);
    List<Bill> findByFilters(LocalDate date, TransactionType transactionType, StoreType storeType);
    List<Bill> findAll();
}