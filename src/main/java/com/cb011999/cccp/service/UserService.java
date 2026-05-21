package com.cb011999.cccp.service;

import com.cb011999.cccp.domain.model.Employee;
import com.cb011999.cccp.domain.model.OnlineCustomer;

import java.util.List;
import java.util.Optional;

public interface UserService {

    RegistrationResult registerCustomer(String name, String contact, String email,
                                        String password, String address);

    LoginResult loginWithEmail(String email, String password);
    
    RegistrationResult registerEmployee(String name, String contact,
            String employeeNumber, String role, String password);

    EmployeeLoginResult loginEmployee(String employeeNumber, String password);

    List<OnlineCustomer> getAllCustomers();

    List<Employee> getAllEmployees();

    Optional<Employee> findEmployeeByNumber(String employeeNumber);

    public static class RegistrationResult {
        private final boolean success;
        private final String message;
        private final OnlineCustomer customer;

        public RegistrationResult(boolean success, String message, OnlineCustomer customer) {
            this.success = success;
            this.message = message;
            this.customer = customer;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public OnlineCustomer getCustomer() { return customer; }
    }

    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final OnlineCustomer customer;

        public LoginResult(boolean success, String message, OnlineCustomer customer) {
            this.success = success;
            this.message = message;
            this.customer = customer;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public OnlineCustomer getCustomer() { return customer; }
    }
    
    public static class EmployeeLoginResult {
        private final boolean success;
        private final String message;
        private final Employee employee;

        public EmployeeLoginResult(boolean success, String message, Employee employee) {
            this.success = success;
            this.message = message;
            this.employee = employee;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Employee getEmployee() { return employee; }
    }
}