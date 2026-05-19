package com.cb011999.cccp.factory;

import com.cb011999.cccp.repository.*;
import com.cb011999.cccp.repository.impl.*;

public class RepositoryFactory {
    
    // Always uses db
    private static boolean useDatabaseRepositories = true;
    
    public static void setUseDatabaseRepositories(boolean useDatabase) {
        useDatabaseRepositories = useDatabase;
    }
    
    public static ItemRepository createItemRepository() {
        if (useDatabaseRepositories) {
            return DatabaseItemRepository.getInstance();
        } else {
            return InMemoryItemRepository.getInstance();
        }
    }
    
    public static BillRepository createBillRepository() {
    	if (useDatabaseRepositories) {
            return DatabaseBillRepository.getInstance();
        } else {
        	return InMemoryBillRepository.getInstance();
        }
    }
    
    public static InventoryRepository createInventoryRepository() {
    	if (useDatabaseRepositories) {
        return DatabaseInventoryRepository.getInstance();
    	} else {
    		return InMemoryInventoryRepository.getInstance();
    	}
    }
    
    public static UserRepository createUserRepository() {
        if (useDatabaseRepositories) {
            return DatabaseUserRepository.getInstance();
        } else {
            return InMemoryUserRepository.getInstance();
        }
    }
    
    public static boolean isUsingDatabaseRepositories() {
        return useDatabaseRepositories;
    }
}