package com.cb011999.cccp.repository.impl;

import com.cb011999.cccp.domain.model.User;
import com.cb011999.cccp.domain.model.OnlineCustomer;
import com.cb011999.cccp.domain.model.Employee;
import com.cb011999.cccp.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation of UserRepository.
 * Uses Singleton pattern.
 */
public class InMemoryUserRepository implements UserRepository {
    private static InMemoryUserRepository instance;
    private final Map<String, User> users;
    
    private InMemoryUserRepository() {
        this.users = new HashMap<>();
    }
    
    public static synchronized InMemoryUserRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryUserRepository();
        }
        return instance;
    }
    
    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }
    
    @Override
    public Optional<OnlineCustomer> findCustomerByEmail(String email) {
        return users.values().stream()
            .filter(user -> user instanceof OnlineCustomer)
            .map(user -> (OnlineCustomer) user)
            .filter(customer -> email.equals(customer.getEmail()))
            .findFirst();
    }
    
    @Override
    public Optional<Employee> findEmployeeByNumber(String employeeNumber) {
        return users.values().stream()
            .filter(user -> user instanceof Employee)
            .map(user -> (Employee) user)
            .filter(emp -> employeeNumber.equals(emp.getEmployeeNumber()))
            .findFirst();
    }
    
    @Override
    public void save(User user) {
        users.put(user.getId(), user);
    }
    
    @Override
    public List<OnlineCustomer> findAllCustomers() {
        return users.values().stream()
            .filter(user -> user instanceof OnlineCustomer)
            .map(user -> (OnlineCustomer) user)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Employee> findAllEmployees() {
        return users.values().stream()
            .filter(user -> user instanceof Employee)
            .map(user -> (Employee) user)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean exists(String id) {
        return users.containsKey(id);
    }
    
    @Override
    public boolean emailExists(String email) {
        return users.values().stream()
            .filter(user -> user instanceof OnlineCustomer)
            .map(user -> (OnlineCustomer) user)
            .anyMatch(customer -> email.equals(customer.getEmail()));
    }
    
    /**
     * Clears all users (useful for testing).
     */
    public void clear() {
        users.clear();
    }
}