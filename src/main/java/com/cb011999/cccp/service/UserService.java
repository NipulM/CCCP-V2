package com.cb011999.cccp.service;

import com.cb011999.cccp.domain.model.Employee;
import com.cb011999.cccp.domain.model.OnlineCustomer;
import com.cb011999.cccp.domain.model.User;
import com.cb011999.cccp.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RegistrationResult registerCustomer(String name, String contact, String email,
                                               String password, String address) {
        // Validate inputs
        if (name == null || name.trim().isEmpty()) {
            return new RegistrationResult(false, "Name is required", null);
        }

        if (contact == null || contact.trim().isEmpty()) {
            return new RegistrationResult(false, "Contact number is required", null);
        }

        if (email == null || email.trim().isEmpty()) {
            return new RegistrationResult(false, "Email is required", null);
        }

        if (!email.contains("@")) {
            return new RegistrationResult(false, "Invalid email address", null);
        }

        if (password == null || password.length() < 8) {
            return new RegistrationResult(false, "Password must be at least 8 characters", null);
        }

        if (address == null || address.trim().isEmpty()) {
            return new RegistrationResult(false, "Delivery address is required", null);
        }

        // Check if email already exists
        if (userRepository.emailExists(email)) {
            return new RegistrationResult(false, "Email already registered", null);
        }

        // Hash the password before saving
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Create customer
        String id = generateCustomerId();
        OnlineCustomer customer = new OnlineCustomer(id, name, contact, email);
        customer.register(hashedPassword, address);

        // Save to repository
        userRepository.save(customer);

        return new RegistrationResult(true, "Registration successful", customer);
    }

    /**
     * Attempts to log in a customer by verifying their email and password.
     *
     * @param email    the customer's email address
     * @param password the plain-text password to verify against the stored hash
     */
    public LoginResult loginWithEmail(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return new LoginResult(false, "Email is required", null);
        }

        if (password == null || password.trim().isEmpty()) {
            return new LoginResult(false, "Password is required", null);
        }

        Optional<OnlineCustomer> customerOpt = userRepository.findCustomerByEmail(email);

        if (!customerOpt.isPresent()) {
            return new LoginResult(false, "Customer not found with that email", null);
        }

        OnlineCustomer customer = customerOpt.get();

        if (!customer.isRegistered()) {
            return new LoginResult(false, "Customer is not registered", null);
        }

        // Verify the provided password against the stored BCrypt hash
        if (!BCrypt.checkpw(password, customer.getPasswordHash())) {
            return new LoginResult(false, "Invalid password", null);
        }

        return new LoginResult(true, "Login successful", customer);
    }

    public List<OnlineCustomer> getAllCustomers() {
        return userRepository.findAllCustomers();
    }

    public List<Employee> getAllEmployees() {
        return userRepository.findAllEmployees();
    }

    public Optional<Employee> findEmployeeByNumber(String employeeNumber) {
        return userRepository.findEmployeeByNumber(employeeNumber);
    }

    private String generateCustomerId() {
        return "CUST" + System.currentTimeMillis();
    }

    public static class RegistrationResult {
        private final boolean success;
        private final String message;
        private final OnlineCustomer customer;

        public RegistrationResult(boolean success, String message, OnlineCustomer customer) {
            this.success = success;
            this.message = message;
            this.customer = customer;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public OnlineCustomer getCustomer() {
            return customer;
        }
    }

    /**
     * Result of login operation.
     */
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final OnlineCustomer customer;

        public LoginResult(boolean success, String message, OnlineCustomer customer) {
            this.success = success;
            this.message = message;
            this.customer = customer;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public OnlineCustomer getCustomer() {
            return customer;
        }
    }
}