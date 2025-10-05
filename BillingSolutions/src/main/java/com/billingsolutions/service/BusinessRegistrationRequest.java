package com.billingsolutions.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A DTO (Data Transfer Object) that carries the necessary information
 * for registering a new business and its initial admin user.
 */
public class BusinessRegistrationRequest {

    @NotBlank(message = "Business name is required.")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters.")
    private String businessName;

    @NotBlank(message = "GST number is required.")
    // You can add a @Pattern annotation here for specific GST format validation
    private String gstNumber;

    @NotBlank(message = "Owner's email is required.")
    @Email(message = "Please provide a valid email address.")
    private String ownerEmail;

    @NotBlank(message = "An admin username is required.")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
    private String adminUsername;

    @NotBlank(message = "A password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String adminPassword;

    // --- Getters and Setters ---
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }
}
