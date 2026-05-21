package com.cb011999.cccp.test.service;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.cb011999.cccp.domain.model.OnlineCustomer;
import com.cb011999.cccp.repository.impl.InMemoryUserRepository;
import com.cb011999.cccp.service.UserService;
import com.cb011999.cccp.service.impl.UserServiceImpl;
 
public class UserServiceTest {
    
    private UserService userService;
    private InMemoryUserRepository userRepo;
    
    @Before
    public void setUp() {
        userRepo = InMemoryUserRepository.getInstance();
        userRepo.clear();

        userService = new UserServiceImpl(userRepo);
    }
    
    @Test
    public void testRegisterCustomer_Success() {
        // Arrange
        String name = "John Doe";
        String contact = "0771234567";
        String email = "john@example.com";
        String password = "password123";
        String address = "123 Main St";
        
        // Act
        var result = userService.registerCustomer(name, contact, email, password, address);
        
        // Assert
        assertTrue("Registration should succeed", result.isSuccess());
        assertEquals("Should return success message", "Registration successful", result.getMessage());
        assertNotNull("Should return customer", result.getCustomer());
        assertEquals("Customer name should match", name, result.getCustomer().getName());
        assertEquals("Customer email should match", email, result.getCustomer().getEmail());
        assertTrue("Customer should be registered", result.getCustomer().isRegistered());
    }
    
    @Test
    public void testRegisterCustomer_EmptyName() {
        // Arrange
        String name = "";
        String contact = "0771234567";
        String email = "john@example.com";
        String password = "password123";
        String address = "123 Main St";
        
        // Act
        var result = userService.registerCustomer(name, contact, email, password, address);
        
        // Assert
        assertFalse("Registration should fail", result.isSuccess());
        assertEquals("Should return error message", "Name is required", result.getMessage());
        assertNull("Should not return customer", result.getCustomer());
    }
    
    @Test
    public void testRegisterCustomer_NullName() {
        // Act
        var result = userService.registerCustomer(null, "0771234567", "john@example.com", 
                                                   "password123", "123 Main St");
        
        // Assert
        assertFalse("Registration should fail", result.isSuccess());
        assertEquals("Name is required", result.getMessage());
        assertNull(result.getCustomer());
    }
    
    @Test
    public void testRegisterCustomer_EmptyEmail() {
        // Act
        var result = userService.registerCustomer("John", "0771234567", "", 
                                                   "password123", "123 Main St");
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Email is required", result.getMessage());
    }
    
    @Test
    public void testRegisterCustomer_InvalidEmail() {
        // Act - email without @
        var result = userService.registerCustomer("John", "0771234567", "invalidemail", 
                                                   "password123", "123 Main St");
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Invalid email address", result.getMessage());
    }
    
    @Test
    public void testRegisterCustomer_ShortPassword() {
        // Act - password less than 8 characters
        var result = userService.registerCustomer("John", "0771234567", "john@example.com", 
                                                   "pass", "123 Main St");
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Password must be at least 8 characters", result.getMessage());
    }
    
    @Test
    public void testRegisterCustomer_DuplicateEmail() {
        // Arrange - Register first customer
        userService.registerCustomer("John", "0771234567", "john@example.com", 
                                     "password123", "123 Main St");
        
        // Act - Try to register with same email
        var result = userService.registerCustomer("Jane", "0772345678", "john@example.com", 
                                                   "password456", "456 Other St");
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Email already registered", result.getMessage());
    }
    
    
    @Test
    public void testLoginWithEmail_Success() {
        // Arrange
        String email = "john@example.com";
        String password = "password123";
        
        userService.registerCustomer("John", "0771234567", email, 
                                     "password123", "123 Main St");
        
        // Act
        var result = userService.loginWithEmail(email, password);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Login successful", result.getMessage());
        assertNotNull(result.getCustomer());
        assertEquals(email, result.getCustomer().getEmail());
    }
    
    @Test
    public void testLoginWithEmail_NotFound() {
        // Act
        var result = userService.loginWithEmail("notfound@example.com", "invalid_password");
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Customer not found with that email", result.getMessage());
    }
    
    @Test
    public void testLoginWithEmail_EmptyEmail() {
        // Act
        var result = userService.loginWithEmail("", "invalid_password");
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Email is required", result.getMessage());
    }
    
    @Test
    public void testGetAllCustomers_Empty() {
        // Act
        var customers = userService.getAllCustomers();
        
        // Assert
        assertNotNull(customers);
        assertEquals(0, customers.size());
    }
    
    
    @Test
    public void testRegisterAndLoginFlow() {
        // Arrange - Register
        String email = "flow@example.com";
        String password = "password123";
        
        var regResult = userService.registerCustomer("Flow Test", "0771234567", email, 
                                                      "password123", "123 Main St");
        
        assertTrue("Registration should succeed", regResult.isSuccess());
        String customerId = regResult.getCustomer().getId();
        
        
        // Act - Login with Email
        var loginResult2 = userService.loginWithEmail(email, password);
        assertTrue("Login with email should succeed", loginResult2.isSuccess());
        
        // Assert - Both logins return same customer
        assertEquals("Should return same customer", 
                    loginResult2.getCustomer().getId());
    }
}