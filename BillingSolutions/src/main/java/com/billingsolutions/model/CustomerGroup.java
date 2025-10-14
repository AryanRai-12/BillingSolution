package com.billingsolutions.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "customer_groups", uniqueConstraints = {
	    @UniqueConstraint(columnNames = {"name", "business_id"})
	})
public class CustomerGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Group name cannot be empty.")
    private String name;
    
    
    @JsonIgnore // Prevents this from being sent in API responses unnecessarily
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
    public CustomerGroup() {}

    // Required for form binding
    public CustomerGroup(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }
}