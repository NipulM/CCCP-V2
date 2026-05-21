package com.cb011999.cccp.domain.model;

import org.mindrot.jbcrypt.BCrypt;

public class Employee extends User {
    private String employeeNumber;
    private String role;
    private String passwordHash;

    public Employee(String id, String name, String contactNumber,
                    String employeeNumber, String role) {
        super(id, name, contactNumber);

        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee number cannot be null or empty");
        }

        this.employeeNumber = employeeNumber;
        this.role = role;
    }

    // Getters
    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getRole() {
        return role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    // Setters
    public void setRole(String role) {
        this.role = role;
    }

    public void setPassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean verifyPassword(String rawPassword) {
        return this.passwordHash != null && BCrypt.checkpw(rawPassword, this.passwordHash);
    }

    @Override
    public String toString() {
        return String.format("Employee[%s, %s, %s]",
                getEmployeeNumber(), getName(), role);
    }
}