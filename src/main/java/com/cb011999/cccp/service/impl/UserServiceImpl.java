package com.cb011999.cccp.service.impl;

import com.cb011999.cccp.domain.model.Employee;
import com.cb011999.cccp.domain.model.OnlineCustomer;
import com.cb011999.cccp.domain.model.User;
import com.cb011999.cccp.repository.UserRepository;
import com.cb011999.cccp.service.UserService;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public RegistrationResult registerCustomer(String name, String contact, String email,
                                               String password, String address) {
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
        if (userRepository.emailExists(email)) {
            return new RegistrationResult(false, "Email already registered", null);
        }

        String id = generateCustomerId();
        OnlineCustomer customer = new OnlineCustomer(id, name, contact, email);
        customer.register(password, address);
        userRepository.save(customer);

        return new RegistrationResult(true, "Registration successful", customer);
    }

    @Override
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

    @Override
    public List<OnlineCustomer> getAllCustomers() {
        return userRepository.findAllCustomers();
    }

    @Override
    public List<Employee> getAllEmployees() {
        return userRepository.findAllEmployees();
    }

    @Override
    public Optional<Employee> findEmployeeByNumber(String employeeNumber) {
        return userRepository.findEmployeeByNumber(employeeNumber);
    }

    private String generateCustomerId() {
        return "CUST" + System.currentTimeMillis();
    }
}