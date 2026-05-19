package com.cb011999.cccp.domain.model;

/**
 * Represents an online customer who shops through the website.
 * Extends User with online-specific properties.
 */
public class OnlineCustomer extends User {
    private String email;
    private String passwordHash;
    private String deliveryAddress;
    private boolean isRegistered;
    
    /**
     * Creates a new online customer.
     * @param id Customer ID
     * @param name Customer name
     * @param contactNumber Contact number
     * @param email Email address
     */
    public OnlineCustomer(String id, String name, String contactNumber, String email) {
        super(id, name, contactNumber);
        
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        this.email = email;
        this.isRegistered = false;
    }
    
    /**
     * Registers the customer with a password.
     * @param passwordHash Hashed password
     * @param deliveryAddress Delivery address
     */
    public void register(String passwordHash, String deliveryAddress) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address cannot be null or empty");
        }
        
        this.passwordHash = passwordHash;
        this.deliveryAddress = deliveryAddress;
        this.isRegistered = true;
    }
    
    /**
     * Verifies password (simplified - in real system use proper hashing).
     * @param passwordHash Password hash to verify
     * @return true if password matches
     */
    public boolean verifyPassword(String passwordHash) {
        return this.passwordHash != null && this.passwordHash.equals(passwordHash);
    }
    
    // Getters
    public String getEmail() {
        return email;
    }

    /**
     * Returns the password hash for persistence only (e.g. repository layer).
     * Do not expose to UI or logs.
     */
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public boolean isRegistered() {
        return isRegistered;
    }
    
    // Setters
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        this.email = email;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public void updatePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        this.passwordHash = newPasswordHash;
    }
    
    @Override
    public boolean isValid() {
        return super.isValid() && email != null && !email.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("OnlineCustomer[%s, %s, Registered: %s]", 
            getId(), getName(), isRegistered);
    }
}