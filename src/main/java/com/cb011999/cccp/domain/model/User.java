package com.cb011999.cccp.domain.model;

public abstract class User {
    private final String id;
    private String name;
    private String contactNumber;
    
    protected User(String id, String name, String contactNumber) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        
        this.id = id;
        this.name = name;
        this.contactNumber = contactNumber;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    // Setters
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        this.name = name;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() 
            && name != null && !name.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id.equals(user.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %s)", name, id);
    }
}