package com.cb011999.cccp.repository;

import com.cb011999.cccp.domain.model.User;
import com.cb011999.cccp.domain.model.OnlineCustomer;
import com.cb011999.cccp.domain.model.Employee;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entities.
 * Handles both OnlineCustomers and Employees.
 */
public interface UserRepository {

    Optional<User> findById(String id);
    Optional<OnlineCustomer> findCustomerByEmail(String email);
    Optional<Employee> findEmployeeByNumber(String employeeNumber);
    
    List<OnlineCustomer> findAllCustomers();
    List<Employee> findAllEmployees();

    boolean exists(String id);
    boolean emailExists(String email);
    
    void save(User user);
}